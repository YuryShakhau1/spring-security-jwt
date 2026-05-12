const showHideElement = (id, show) => {
    const errorMessageElement = document.getElementById(id);
    if (show) {
        errorMessageElement.style.display = 'block';
    } else {
        errorMessageElement.style.display = 'none';
    }
};
