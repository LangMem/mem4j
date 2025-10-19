# Intelligent Memory Management

## Overview

The `add` method in Mem4j now features intelligent memory management that automatically decides whether to INSERT, UPDATE, DELETE, or SKIP memories based on their similarity to existing memories and LLM-driven analysis.

## How It Works

When adding new memories, the system:

1. **Extracts memories** from conversations using LLM
2. **Searches for similar existing memories** in the vector database (similarity threshold: 0.7)
3. **Makes intelligent decisions** based on similarity scores:
   - **Score > 0.95**: SKIP (near-duplicate)
   - **Score 0.85-0.95**: Consult LLM to decide UPDATE/DELETE/SKIP/INSERT
   - **Score 0.7-0.85**: INSERT as separate memory
   - **No similar memories**: INSERT new memory

## Decision Logic

### INSERT
- New memory is sufficiently different from existing memories
- Adds complementary information that should be kept separate
- Default action when no similar memories exist

### UPDATE
- New memory contains more recent or accurate information
- LLM merges the new and existing memory content
- Preserves the memory ID and updates the content
- Example: "User likes coffee" → "User prefers tea over coffee"

### DELETE
- New memory contradicts or makes existing memory obsolete
- Old information is removed completely
- Example: "User is a developer" → "User is no longer a developer"

### SKIP
- New memory is essentially a duplicate
- No new information would be added
- Avoids redundancy in the memory store

## LLM Decision Prompt

The system uses a structured prompt to ask the LLM to analyze memory pairs:

```
You are a memory management system. Compare these two memories and decide what action to take:

EXISTING MEMORY: [existing content]
NEW MEMORY: [new content]

Analyze and decide:
1. UPDATE: [merged content] - if new memory contains updated information
2. DELETE - if new memory makes existing memory obsolete
3. SKIP - if new memory adds no value
4. INSERT - if they are complementary but distinct

Consider:
- Temporal context (newer information may supersede older)
- Specificity (more specific information may update general information)
- Contradictions (direct contradictions should trigger DELETE + INSERT)
- Redundancy (avoid storing the same information twice)
```

## Benefits

1. **Reduces redundancy**: Avoids storing duplicate or near-duplicate information
2. **Keeps information current**: Updates memories when newer information arrives
3. **Maintains coherence**: Removes contradictory or obsolete memories
4. **Saves storage**: Only stores valuable, distinct memories
5. **Improves search quality**: Cleaner memory store leads to better retrieval

## Usage Example

```java
Memory memory = new Memory(config, vectorStore, llmService, embeddingService);

// Initial conversation
List<Message> messages1 = Arrays.asList(
    new Message("user", "I love drinking coffee"),
    new Message("assistant", "I'll remember that!")
);
memory.add(messages1, userId);
// Result: 1 inserted

// Similar conversation (will skip)
List<Message> messages2 = Arrays.asList(
    new Message("user", "I really enjoy coffee"),
    new Message("assistant", "Yes, you mentioned that!")
);
memory.add(messages2, userId);
// Result: 1 skipped (duplicate)

// Updated preference (will update)
List<Message> messages3 = Arrays.asList(
    new Message("user", "I've switched to tea instead of coffee"),
    new Message("assistant", "I'll update that!")
);
memory.add(messages3, userId);
// Result: 1 updated (coffee → tea)
```

## Configuration

The intelligent decision-making uses the standard similarity threshold from the configuration:

```yaml
mem4j:
  similarity-threshold: 0.7  # Used for finding similar memories
```

## Performance Considerations

- **Search overhead**: Each new memory triggers a similarity search (5 results max)
- **LLM calls**: Only for memories with similarity 0.85-0.95 (most cases use rule-based decisions)
- **Fallback behavior**: If LLM fails, defaults to INSERT to avoid data loss

## Monitoring

The system logs detailed information about memory operations:

```
INFO: Memory operations for user john: 2 inserted, 1 updated, 0 deleted, 1 skipped (total extracted: 4)
DEBUG: Inserting new memory: 'User lives in Seattle' - Reason: No similar memories found
DEBUG: Updating existing memory 'User likes coffee' -> 'User prefers tea' - Reason: LLM decided to merge
DEBUG: Skipping memory: 'User enjoys coffee' - Reason: Memory is nearly identical (score: 0.96)
```

## Testing

Comprehensive test coverage includes:
- Insert new memory when no similar exists
- Skip near-duplicates (score > 0.95)
- Update memory when LLM decides
- Delete memory when LLM decides
- Insert separate memory for moderate similarity
- Multiple memories with different actions
- LLM error fallback to INSERT

See `IntelligentMemoryTest.java` for full test suite.

## Future Enhancements

Potential improvements for future versions:
- Configurable similarity thresholds for each action
- Batch processing for multiple memories
- User-configurable decision strategies
- Memory merge history tracking
- Async LLM decision-making
