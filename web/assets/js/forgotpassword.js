async function sendVerificationCode() {
    let email = document.getElementById("email").value.trim();
    let message = document.getElementById("message");
    let sendBtn = document.getElementById("sendCodeBtn");

    // Validate email input
    if (!email) {
        message.innerText = "Please enter your email.";
        return;
    }

    try {
        // Show sending state
        sendBtn.innerText = "Sending...";
        sendBtn.disabled = true;
        message.innerText = "";

        // Send request to server
        const response = await fetch("ForgotPassword", {
            method: "POST",
            body: JSON.stringify({email: email}),
            headers: {"Content-Type": "application/json"}
        });

        if (response.ok) {
            const json = await response.json();
            console.log(json);

            if (json.status) {
                // ✅ Successfully sent
                message.innerText = json.message || "✅ Verification code sent to your email.";
                document.getElementById("verificationSection").style.display = "block";

                // Enable password section after correct code entered
                document.getElementById("verification").addEventListener("input", function () {
                    if (this.value.length === 6) { // Example 6-digit verification code
                        document.getElementById("passwordSection").style.display = "block";
                    }
                });
            } else {
                // ❌ Backend says failure
                message.innerText = json.message || "Failed to send verification code.";
            }
        } else {
            message.innerText = "Server error. Please try again.";
            return;
        }


    } catch (error) {
        console.error(error);
        message.innerText = "An error occurred. Please try again.";
    } finally {
        // Reset button state
        sendBtn.innerText = "Send Verification Code";
        sendBtn.disabled = false;
    }
}

async function savePassword() {
    let email = document.getElementById("email").value.trim();
    let verificationCode = document.getElementById("verification").value.trim();
    let newPassword = document.getElementById("newPassword").value.trim();
    let confirmPassword = document.getElementById("confirmPassword").value.trim();
    let message = document.getElementById("message");

    // Basic validation before sending
    if (!email) {
        message.innerText = "Email is required.";
        return;
    }
    if (!verificationCode) {
        message.innerText = "Verification code is required.";
        return;
    }
    if (newPassword !== confirmPassword) {
        message.innerText = "Passwords do not match.";
        return;
    }

    try {
        // Disable button while processing
        const updateBtn = event.target;
        updateBtn.disabled = true;
        updateBtn.innerText = "Updating...";

        const response = await fetch("VerifyPassword", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                email: email,
                verificationCode: verificationCode,
                newPassword: newPassword,
                confirmPassword: confirmPassword
            })
        });

        if (response.ok) {
            const json = await response.json();
            console.log(json);

            if (json.status) {
                message.style.color = "green";
                message.innerText = json.message || "Password updated successfully!";
                alert(json.message || "Password updated successfully!");

                window.location = "index.html";

            } else {
                message.style.color = "red";
                message.innerText = json.message || "Failed to update password.";
                const updateBtn = event.target;
                updateBtn.disabled = false;
                updateBtn.innerText = "Update Password";
            }
        } else {
            message.innerText = "Server error. Please try again.";
            const updateBtn = event.target;
            updateBtn.disabled = false;
            updateBtn.innerText = "Update Password";
        }

    } catch (error) {
        console.error("Error updating password:", error);
        message.innerText = "An error occurred. Please try again.";
    } finally {
        // Reset button state
        const updateBtn = event.target;
        updateBtn.disabled = false;
        updateBtn.innerText = "Update Password";
    }
}


async function verifyAccount() {

    const email = document.getElementById("email").value;
    const verificationCode = document.getElementById("verificationCode").value;

    const verification = {

        email: email,

        verificationCode: verificationCode
    };


    const verificationJSON = JSON.stringify(verification);
    console.log(verificationJSON);
    const response = await fetch(
            "VerifyAccount",
            {
                method: "POST",
                body: verificationJSON,
                headers: {
                    "Content-Type": "application/json"
                }
            }

    );
    if (response.ok) {
        const json = await response.json();
        console.log(json);
        if (json.status) {// if true

            window.location = "index.html";
        } else { // when status false

            if (json.message === '1') { // Email not found

                window.location = "sign-in.html";

            } else {

            }
            document.getElementById("message").innerHTML = json.message;

        }
    } else {

        document.getElementById("message").innerHTML = "Verification failed. Please try again later!";
    }

}



async function resendCode() {
    const popup = Notification();
    const response = await fetch("ResendCode");

    if (response.ok) {

        const json = await response.json();
        if (json.status) {


        } else {

            if (json.message === '1') { // Email not found

                window.location = "sign-in.html";

            } else {
                popup.error(
                        {
                            message: json.message

                        });
            }
        }

    } else {
        popup.error(
                {
                    message: json.message

                });
    }
}
