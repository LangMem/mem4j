import { get } from '../axios.ts'

// post
export const test = () => get('/api/v1/demo-controller/test', null)
