<template>
  <div class="main-container">
    <div class="img-container container-item">
      <img src="src/assets/404.svg">
    </div>
    <div class="text-container container-item">
      <div class="code">404</div>
      <div class="msg">从前有座山，山上有座庙，庙里有个页面，现在找不到.</div>
      <div class="action" @click="$router.push('/')">返回首页,查看更多内容.</div>
    </div>
  </div>
</template>

<style scoped>
* {
  padding: 0;
  margin: 0;
}

html,
body {
  height: 100%;
}

body {
  background-color: rgba(223, 223, 255, 0.39);
  display: flex;
  justify-content: center;
  align-items: center;
}

.main-container {
  margin: auto;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 80%;
  max-width: 1000px;
  max-height: 500px;
  min-width: 600px;
  background-color: white;
  font-size: 0;
  border-radius: 20px;
  box-shadow: 0 0 50px 0 rgba(146, 146, 146, 0.63);
}

.main-container .container-item {
  display: inline-block;
  vertical-align: middle;
  width: 50%;
}

.main-container .img-container {
  background-color: rgba(253, 216, 168, 0.692);
  border-top-left-radius: 20px;
  border-bottom-left-radius: 20px;
}

.main-container .text-container .code {
  font-size: clamp(150px, 20vw, 200px);
  font-family: 'Arial Narrow';
  color: rgb(86, 86, 253);
  font-weight: bolder;
  text-align: center;
}

.main-container .text-container .msg {
  font-size: 18px;
  text-align: center;
  font-weight: 200;
  margin-bottom: 20px;
}

.main-container .text-container .action {
  font-size: 15px;
  font-weight: 200;
  text-align: center;
  text-decoration-line: underline;
  cursor: pointer;
}
</style>