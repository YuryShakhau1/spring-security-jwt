const firstNameInput = document.getElementById('first-name');
const lastNameInput = document.getElementById('last-name');
const emailInput = document.getElementById('email');
const passwordInput = document.getElementById('password');
const repeatPasswordInput = document.getElementById('repeat-password');
const registerBtn = document.getElementById('register-button');

const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

const login = async () => {
    const firstName = firstNameInput.value;
    const lastName = lastNameInput.value;
    const email = emailInput.value;
    const password = passwordInput.value;
    const repeatPassword = repeatPasswordInput.value;

    let errorMessage = '';
    if (!emailRegex.test(email)) {
        errorMessage = 'Please enter a valid email address (e.g. name@mail.com)';
    }

    if (password !== repeatPassword) {
        errorMessage = 'Password and repeat password are different';
    }

    if (errorMessage.length > 0) {
        showHideElement('error-message', true);
        showErrorMessage('error-message-block', true, errorMessage);
        return;
    }

    const response = await fetch('../users/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            firstName: firstName,
            lastName: lastName,
            email: email,
            password: password
        }),
        credentials: 'include'
    });

    if (!response.ok) {
        showHideElement('error-message', true);
        showErrorMessage('error-message-block', true, await response.text());
        return;
    }

    const accessToken = await response.text();
    window.location.href = './login.html';
};

const showErrorMessage = (id, show, message) => {
    const errorMessageElement = document.getElementById(id);
    if (show) {
        errorMessageElement.style.display = 'block';
        errorMessageElement.innerHTML = message;
    } else {
        errorMessageElement.style.display = 'none';
        errorMessageElement.innerHTML = '';
    }
};

const hideErrorMessage = () => showHideElement('error-message', false);
firstNameInput.addEventListener('click', hideErrorMessage);
lastNameInput.addEventListener('click', hideErrorMessage);
emailInput.addEventListener('click', hideErrorMessage);
passwordInput.addEventListener('click', hideErrorMessage);
repeatPasswordInput.addEventListener('click', hideErrorMessage);

registerBtn.addEventListener('click', () => login());