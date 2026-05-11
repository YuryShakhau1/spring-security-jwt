const updateUsers = async () => {
    const accessToken = localStorage.getItem('accessToken');
    const response = await fetch('../users/search', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${accessToken}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            pageNumber: 0,
            pageSize: 50
        }),
        credentials: 'include'
    });

    const pageable = await response.json();
    const users = pageable.content;
    buildUsers(users);
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
    const usersElement = document.getElementById('users-content');

    usersElement.innerHTML = users.map(user =>
    `<div><span>${user.firstName} ${user.lastName}</span><span><button onclick="deleteUser(${user.id})">Delete</button></span></div>`).join('');
};

updateUsers();