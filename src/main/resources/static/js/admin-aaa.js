let pageNumber = 0;
let pageSize = 2;

const updateUsers = async () => {
    const accessToken = localStorage.getItem('accessToken');
    const response = await fetch('../users/search', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${accessToken}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            pageNumber: pageNumber,
            pageSize: pageSize
        }),
        credentials: 'include'
    });

    const json = await response.json();
    const users = json.content;
    buildUsers(users);
    buildPageNumbers(json.pageable.totalPages);
};

const deleteUser = async (userId) => {
    const accessToken = localStorage.getItem('accessToken');
    const response = await fetch('../users', {
        method: 'DELETE',
        headers: {
            'Authorization': `Bearer ${accessToken}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            userId: userId
        }),
        credentials: 'include'
    });
    await response.text();

    updateUsers();
};

const buildUsers = (users) => {
    const usersElement = document.getElementById('users-container');

    usersElement.innerHTML = users.map(user =>
    `<div>
    <span>${user.firstName} ${user.lastName} ${user.email}</span>
    <span><button onclick="deleteUser(${user.id})">Delete</button></span>
    </div>`).join('');
};

const buildPageNumbers = (size) => {
    const pageNumbersElement = document.getElementById('page-numbers-container');
    pageNumbersElement.replaceChildren();

    const fragment = document.createDocumentFragment();
    for (let i = 0; i < size; i++) {
        const btn = document.createElement('button');
        btn.textContent = i + 1;

        btn.onclick = function() {
            pageNumber = i;
            updateUsers();
        };

        fragment.appendChild(btn);
    }

    pageNumbersElement.appendChild(fragment);
};

updateUsers();