// 只能为数字验证
export function isNumber(_: any, value: string, callback: any) {
    let number = /^\d+$|^\d+[.]?\d+$/
    if (!number.test(value)) {
        callback(new Error("格式有误,只能为数字"));
    } else {
        callback();
    }
}

export function isNumberOrzim(_: any, value: string, callback: any) {
    let number = /^[\da-z]+$/i
    if (!number.test(value)) {
        callback(new Error("格式有误,只能为数字和字母"));
    } else {
        callback();
    }
}

// 去除空格
export function isSpace(_: any, value: string) {
    return value.replace(/\s*/g, "");
}

// 只能为中文校验
export function isChinese(_: any, value: string, callback: any) {
    let reg = /^[\u4e00-\u9fa5]+$/
    if (!reg.test(value)) {
        callback(new Error("格式有误,只能为中文"));
    } else {
        callback();
    }
}

export function isnull(_: any, value: string, callback: any) {
    if (value.replace(/(^s*)|(s*$)/g, "").length === 0) {
        callback(new Error("不能为空"));
    } else {
        callback();
    }
}

// 特殊符号验证
export function checkSpecificKey(str: string) {
    let specialKey = "[`%~!$^&*()=|{}':;',\\[\\].<>/?~！￥……&*（）——|{}【】‘；：”“'。，、？]‘'";
    for (var i = 0; i < str.length; i++) {
        if (specialKey.indexOf(str.substr(i, 1)) !== -1) {
            return false;
        }
    }
    return true;
}

// 特殊符号验证
export function isSymbol(_: any, value: string, callback: any) {
    if (!checkSpecificKey(value)) {
        callback(new Error("格式有误,不支持特殊符号"));
    } else {
        callback();
    }
}

//密码长度不小于8位
export function validatePassword(_: any, value: string, callback: any) {
    if (value.length < 8) {
        callback(new Error('密码最少为8位字符！'))
    } else {
        callback()
    }
}

//验证一个固定位数的数字编号
export function validateNumber(_: any, value: string, callback: any) {
    if (value.length !== 5) {
        callback(new Error('部门编号不能多于或少于5位'))
    } else if (value.length === null) {
        callback(new Error('密码最少为6位字符！'))
    } else {
        callback()
    }
}


/*是否合法IP地址*/
export function validateIP(_: any, value: string, callback: any) {
    if (value === '' || value === undefined || value === null) {
        callback();
    } else {
        const reg = /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/;
        if ((!reg.test(value)) && value !== '') {
            callback(new Error('请输入正确的IP地址'));
        } else {
            callback();
        }
    }
}

/*密码复杂度*/
export function validatePasswordSpup(_: any, value: string, callback: any) {
    if (value === '' || value === undefined || value === null) {
        callback();
    } else {
        const reg = new RegExp('(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])(?=.*[^a-zA-Z0-9]).{8,30}');
        if ((!reg.test(value)) && value !== '') {
            callback(new Error('您的密码复杂度过于简单'));
        } else {
            callback();
        }
    }
}


/* 是否手机号码或者固话*/
export function validatePhoneTwo(_: any, value: string, callback: any) {
    const reg = /^((0\d{2,3}-\d{7,8})|(1[34578]\d{9}))$/;
    if (value === '' || value === undefined || value === null) {
        callback();
    } else {
        if ((!reg.test(value)) && value !== '') {
            callback(new Error('请输入正确的电话号码或者固话号码'));
        } else {
            callback();
        }
    }
}
/* 是否固话*/
export function validateTelphone(_: any, value: string, callback: any) {
    const reg = /0\d{2}-\d{7,8}/;
    if (value === '' || value === undefined || value === null) {
        callback();
    } else {
        if ((!reg.test(value)) && value !== '') {
            callback(new Error('请输入正确的固话（格式：区号+号码,如010-1234567）'));
        } else {
            callback();
        }
    }
}
/* 是否手机号码*/
export function validatePhone(_: any, value: string, callback: any) {
    const reg = /^[1][3,4,5,7,8][0-9]{9}$/;
    if (value === '' || value === undefined || value === null) {
        callback();
    } else {
        if ((!reg.test(value)) && value !== '') {
            callback(new Error('请输入正确的电话号码'));
        } else {
            callback();
        }
    }
}
/* 是否身份证号码*/
export function validateIdNo(_: any, value: string, callback: any) {
    const reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
    if (value === '' || value === undefined || value === null) {
        callback();
    } else {
        if ((!reg.test(value)) && value !== '') {
            callback(new Error('请输入正确的身份证号码'));
        } else {
            callback();
        }
    }
}
/* 是否邮箱*/
export function validateEMail(_: any, value: string, callback: any) {
    const reg = /^([a-zA-Z0-9]+[-_\.]?)+@[a-zA-Z0-9]+\.[a-z]+$/;
    if (value === '' || value === undefined || value === null) {
        callback();
    } else {
        if (!reg.test(value)) {
            callback(new Error('请输入正确的邮箱地址'));
        } else {
            callback();
        }
    }
}
/* 合法uri*/
export function validateURL(_: any, value: string, callback: any) {
    if (value === '' || value === undefined || value === null) {
        callback();
    } else {
        const urlregex = /^([a-zA-Z0-9.-]+(:[a-zA-Z0-9.&%$-]+)*@)*((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]?)(\.(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3}|([a-zA-Z0-9-]+\.)*[a-zA-Z0-9-]+\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(:[0-9]+)*(\/($|[a-zA-Z0-9.,?'\\+&%$#=~_-]+))*$/;
        if ((!urlregex.test(value)) && value !== '') {
            callback(new Error('请输入正确的网站域名'));
        } else {
            callback();
        }
    }
}

/*验证内容是否英文数字以及下划线*/
export function isPassword(_: any, value: string, callback: any) {
    const reg = /^[_a-zA-Z0-9]+$/;
    if (value === '' || value === undefined || value === null) {
        callback();
    } else {
        if (!reg.test(value)) {
            callback(new Error('密码仅由英文字母，数字以及下划线组成'));
        } else {
            callback();
        }
    }
}

/*自动检验数值的范围*/
export function checkMax20000(_: any, value: number, callback: any) {
    if (String(value) === '' || value === undefined || value === null) {
        callback();
    } else if (!Number(value)) {
        callback(new Error('请输入[1,20000]之间的数字'));
    } else if (value < 1 || value > 20000) {
        callback(new Error('请输入[1,20000]之间的数字'));
    } else {
        callback();
    }
}

//验证数字输入框最大数值,32767
export function checkMaxVal(_: any, value: number, callback: any) {
    if (value < 0 || value > 32767) {
        callback(new Error('请输入[0,32767]之间的数字'));
    } else {
        callback();
    }
}

//验证是否1-99之间
export function isOneToNinetyNine(_: any, value: string, callback: any) {
    if (!value) {
        return callback(new Error('输入不可以为空'));
    }
    setTimeout(() => {
        if (!Number(value)) {
            callback(new Error('请输入正整数'));
        } else {
            const re = /^[1-9][0-9]{0,1}$/;
            const rsCheck = re.test(value);
            if (!rsCheck) {
                callback(new Error('请输入正整数，值为【1,99】'));
            } else {
                callback();
            }
        }
    }, 0);
}

// 验证是否整数
export function isInteger(_: any, value: string, callback: any) {
    if (!value) {
        return callback(new Error('输入不可以为空'));
    }
    setTimeout(() => {
        if (!Number(value)) {
            callback(new Error('请输入正整数'));
        } else {
            const re = /^[0-9]*[1-9][0-9]*$/;
            const rsCheck = re.test(value);
            if (!rsCheck) {
                callback(new Error('请输入正整数'));
            } else {
                callback();
            }
        }
    }, 0);
}

// 验证是否整数,非必填
export function isIntegerNotMust(_: any, value: string, callback: any) {
    if (!value) {
        callback();
    }
    setTimeout(() => {
        if (!Number(value)) {
            callback(new Error('请输入正整数'));
        } else {
            const re = /^[0-9]*[1-9][0-9]*$/;
            const rsCheck = re.test(value);
            if (!rsCheck) {
                callback(new Error('请输入正整数'));
            } else {
                callback();
            }
        }
    }, 1000);
}

// 验证是否是[0-1]的小数
export function isDecimal(_: any, value: string, callback: any) {
    if (!value) {
        return callback(new Error('输入不可以为空'));
    }
    setTimeout(() => {
        if (!Number(value)) {
            callback(new Error('请输入[0,1]之间的数字'));
        } else {
            if (Number(value) < 0 || Number(value) > 1) {
                callback(new Error('请输入[0,1]之间的数字'));
            } else {
                callback();
            }
        }
    }, 100);
}

// 验证是否是[1-10]的小数,即不可以等于0
export function isBtnOneToTen(_: any, value: string, callback: any) {
    if (typeof value == 'undefined') {
        return callback(new Error('输入不可以为空'));
    }
    setTimeout(() => {
        if (!Number(value)) {
            callback(new Error('请输入正整数，值为[1,10]'));
        } else {
            if (!(value === '1' || value === '2' || value === '3' || value === '4' || value === '5' || value === '6' || value === '7' || value === '8' || value === '9' || value === '10')) {
                callback(new Error('请输入正整数，值为[1,10]'));
            } else {
                callback();
            }
        }
    }, 100);
}
// 验证是否是[1-100]的小数,即不可以等于0
export function isBtnOneToHundred(_: any, value: string, callback: any) {
    if (!value) {
        return callback(new Error('输入不可以为空'));
    }
    setTimeout(() => {
        if (!Number(value)) {
            callback(new Error('请输入整数，值为[1,100]'));
        } else {
            if (Number(value) < 1 || Number(value) > 100) {
                callback(new Error('请输入整数，值为[1,100]'));
            } else {
                callback();
            }
        }
    }, 100);
}
// 验证是否是[0-100]的小数
export function isBtnZeroToHundred(_: any, value: string, callback: any) {
    if (!value) {
        return callback(new Error('输入不可以为空'));
    }
    setTimeout(() => {
        if (!Number(value)) {
            callback(new Error('请输入[1,100]之间的数字'));
        } else {
            if (Number(value) < 0 || Number(value) > 100) {
                callback(new Error('请输入[1,100]之间的数字'));
            } else {
                callback();
            }
        }
    }, 100);
}

// 验证端口是否在[0,65535]之间
export function isPort(_: any, value: string, callback: any) {
    if (!value) {
        return callback(new Error('输入不可以为空'));
    }
    setTimeout(() => {
        if (value === '' || typeof (value) === undefined) {
            callback(new Error('请输入端口值'));
        } else {
            const re = /^([0-9]|[1-9]\d|[1-9]\d{2}|[1-9]\d{3}|[1-5]\d{4}|6[0-4]\d{3}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/;
            const rsCheck = re.test(value);
            if (!rsCheck) {
                callback(new Error('请输入在[0-65535]之间的端口值'));
            } else {
                callback();
            }
        }
    }, 100);
}
// 验证端口是否在[0,65535]之间，非必填,isMust表示是否必填
export function isCheckPort(_: any, value: string, callback: any) {
    if (!value) {
        callback();
    }
    setTimeout(() => {
        if (value === '' || typeof (value) === undefined) {
            //callback(new Error('请输入端口值'));
        } else {
            const re = /^([0-9]|[1-9]\d|[1-9]\d{2}|[1-9]\d{3}|[1-5]\d{4}|6[0-4]\d{3}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/;
            const rsCheck = re.test(value);
            if (!rsCheck) {
                callback(new Error('请输入在[0-65535]之间的端口值'));
            } else {
                callback();
            }
        }
    }, 100);
}

/* 小写字母*/
export function validateLowerCase(str: string) {
    const reg = /^[a-z]+$/;
    return reg.test(str);
}
/*保留2为小数*/
export function validatetoFixedNew(str: string) {
    return str;
}
/* 验证key*/
// export function validateKey(str) {
//     var reg = /^[a-z_\-:]+$/;
//     return reg.test(str);
// }

/* 大写字母*/
export function validateUpperCase(str: string) {
    const reg = /^[A-Z]+$/;
    return reg.test(str);
}

/* 大小写字母*/
export function validatAlphabets(str: string) {
    const reg = /^[A-Za-z]+$/;
    return reg.test(str);
}
