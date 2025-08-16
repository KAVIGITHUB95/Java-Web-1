async function verifyAccount(){
    
    const verificationCode = document.getElementById("verification").value;
    
    const verification = {
        verificationCode: verificationCode
    
    };
    
    const verificationJSON = JSON.stringify(verification);
    console.log(verificationJSON);
    const response = await fetch(
            "VerifyAccount",
    {
        method: "PUT",
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
                
                window.location = "signin.html";
            
            } else {
                alert(json.message);
            }
            
            alert(json.message);
        }
    } else {
        alert(json.message || "Verification failed. Please try again later!");
    }
    
}



async function resendCode(){
    const popup = new Notification();
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
