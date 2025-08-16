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

    alertMessage.innerText = message; // Set the message in the alert box
    alertBox.style.display = "block"; // Show the alert

    // Automatically close the alert after 5 seconds
    setTimeout(closeAlert, 1500);
}

// Function to close the alert
function closeAlert() {
    const alertBox = document.getElementById("bottomAlert");
    alertBox.style.display = "none"; // Hide the alert
}
