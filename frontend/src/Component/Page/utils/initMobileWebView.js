export function isWeb() {
    const userAgent = navigator.userAgent.toLowerCase();
    const isAndroidWebView = (userAgent.indexOf('android') > -1 && userAgent.indexOf('mobile') > -1) || userAgent.indexOf('app') > -1;
    return isAndroidWebView
  }

export function checkLocalStorage() {
    return new Promise((resolve, reject) => {
        function getDataFromStorage() {
        return {
            nickname: localStorage.getItem('nickname'),
            userId: localStorage.getItem('userId')
        };
        }

        let { nickname, userId } = getDataFromStorage();

        if (nickname && userId) {
            resolve();  
        } else {
            setTimeout(() => {
                let { nickname, userId } = getDataFromStorage();
                if (nickname && userId) {
                    resolve();
                } else {
                    reject(new Error("계정 정보가 로컬 스토리지에 없습니다."));
                }
            }, 3000);
        }
    });
}