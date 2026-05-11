const refreshToken = async () => {
    const refreshToken = getCookie('refreshToken');
    const response = await fetch('../auth/refresh', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            refreshToken: refreshToken
        }),
        credentials: 'include'
    });

    if (!response.ok) {
        showHideElement('error-message', true);
    }

    await response.text();
    localStorage.setItem('authToken', token);
    window.location.href = './user.html';
};

const tokenToJson = (token) => {
    const base64Url = token.split('.')[0];
    const base64 = base64Url.replace('/-/g', '+').replace('/_/g', '/');

    const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));

    return JSON.parse(jsonPayload);
};

const getCookie = (name) => {
    const cookie = document.cookie;
    const parts = cookie.split(`${name}=`)
    const refreshToken = parts.pop().split(';').shift();
};