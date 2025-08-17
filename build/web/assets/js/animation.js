function animationstart(){
    const loader = document.getElementById("page-loader");
    loader.classList.add("visible");
}

function animationend(){
    const loader = document.getElementById("page-loader");
    setTimeout(() => {
            
        loader.classList.remove("visible");
        }, 300);
}

// Function to show the alert
function showAlert(message) {
    const alertBox = document.getElementById("bottomAlert");
    const alertMessage = document.getElementById("alertMessage");

    alertMessage.innerText = message;
    alertBox.classList.add("show");

    // Automatically close after 1.5s
    setTimeout(() => {
        closeAlert();
    
    }, 6000);

}

// Function to close the alert

function closeAlert() {
    const alertBox = document.getElementById("bottomAlert");
    
    alertBox.classList.remove("show");

}
