const logout = async () => {
    const accessToken = localStorage.getItem('accessToken');
        const response = await fetch('../auth/logout', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${accessToken}`
            }
        });
    if (response.ok) {
        window.location.href = './login.html';
    }
};

const logoutAll = async () => {
    const accessToken = localStorage.getItem('accessToken');
    const response = await fetch('../auth/logout/all', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${accessToken}`
        }
    });
    if (response.ok) {
        window.location.href = './login.html';
    }
};

const updateUserName = async () => {
    const accessToken = localStorage.getItem('accessToken');
    const response = await fetch('../users/me', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${accessToken}`
        }
    });
    if (response.ok) {
        const user = await response.json();
        document.getElementById('first-name').innerHTML = user.firstName;
        document.getElementById('last-name').innerHTML = user.lastName;
    }
};

fetch('./fragment/header.html')
    .then(response => response.text())
    .then(data => {
        document.getElementById('header-container').innerHTML = data;

        updateUserName();

        const logoutBtn = document.getElementById('logout');
        const logoutAllBtn = document.getElementById('logout-all');
        logoutBtn.addEventListener('click', () => logout());
        logoutAllBtn.addEventListener('click', () => logoutAll());
    });
