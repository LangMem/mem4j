import service from './manager.ts'

// 封装get  post 方法
export function get(url: string, params: any) {
    return new Promise((resolve, reject) => {
        service.get(url, {
            params: params,
            // headers: axios.defaults.headers
        }).then(res => {
            resolve(res.data);
        }).catch(err => {
            reject(err.data);
        });
    });
}


export function post(url: string, params: any) {
    return new Promise((resolve, reject) => {
        service.post(url, params, {
            // headers: axios.defaults.headers
        })
            .then(res => {
                resolve(res.data);
            })
            .catch(err => {
                reject(err.data);
            });
    });
}