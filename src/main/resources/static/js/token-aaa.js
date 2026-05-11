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

    const accessToken = await response.text();
    const accessTokenParams = processToken(accessToken);
    localStorage.setItem('accessToken', accessToken);
};

const tokenToJson = (token) => {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace('/-/g', '+').replace('/_/g', '/');

    const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));

    return JSON.parse(jsonPayload);
};

const urlAccordingRole = (tokenParams) => {
    if (tokenParams.roles.includes('ROLE_USER')) {
        return '/user.html';
    }
    if (tokenParams.roles.includes('ROLE_ADMIN') || tokenParams.roles.includes('ROLE_SUPER_ADMIN')) {
        return '/admin.html';
    }
    throw new Error('Valid user role not found');
};

const processToken = (token) => {
    const accessTokenParams = tokenToJson(token);
    const expirationDate = new Date(accessTokenParams.exp * 1000);
    localStorage.setItem('accessTokenExpDate', expirationDate);
    localStorage.setItem('accessTokenParams', accessTokenParams);
    return accessTokenParams;
};