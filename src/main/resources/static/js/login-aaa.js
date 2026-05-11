const login = async () => {
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const response = await fetch('../auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            email: email,
            password: password
        }),
        credentials: 'include'
    });

    if (!response.ok) {
        showHideElement('error-message', true);
    }

    const accessToken = await response.text();
    const accessTokenParams = processToken(accessToken);
    localStorage.setItem('accessToken', accessToken);
    window.location.href = '.' + urlAccordingRole(accessTokenParams);
};

const showHideElement = (id, show) => {
    const errorMessageElement = document.getElementById(id);
    if (show) {
        errorMessageElement.style.display = 'block';
    } else {
        errorMessageElement.style.display = 'none';
    }
};

const emailInput = document.getElementById('email');
const passwordInput = document.getElementById('password');
const loginBtn = document.getElementById('login-button');

emailInput.addEventListener('click', () => showHideElement('error-message', false));
passwordInput.addEventListener('click', () => showHideElement('error-message', false));
emailInput.addEventListener('keyup', (event) => event.keyCode == 13 && login());
passwordInput.addEventListener('keyup', (event) => event.keyCode == 13 && login());
loginBtn.addEventListener('click', () => login());