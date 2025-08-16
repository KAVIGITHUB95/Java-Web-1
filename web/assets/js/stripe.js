// Pure JavaScript Stripe Backend (No Node.js/Express)
// Can be used with Deno, Bun, or serverless functions

// Configuration - Replace with your actual keys
const STRIPE_SECRET_KEY = 'sk_test_51Rt9g7E6u5aJcwrGWCdCaheA5tpGtyP8X7CtKJhX3KV4HZFyNe46E60e2hajq6XH85Nbqs4fo7M3ztx542fn9HIF007l5OCDMs'; // Replace with your secret key
const STRIPE_WEBHOOK_SECRET = 'whsec_VUe7deVdEYbPNd2hAia1Rhc2i8XjeDV9'; // Replace with webhook secret
const FRONTEND_URL = 'https://a7cb2e2a1b45.ngrok-free.app/KickCart/Stripe.html'; // Replace with your domain

// Stripe API helper function
async function stripeRequest(endpoint, data, method = 'POST') {
    const response = await fetch(`https://api.stripe.com/v1/${endpoint}`, {
        method: method,
        headers: {
            'Authorization': `Bearer ${STRIPE_SECRET_KEY}`,
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams(data).toString()
    });

    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error?.message || 'Stripe API error');
    }

    return await response.json();
}

// CORS helper
function setCORSHeaders(headers = {}) {
    return {
        ...headers,
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
        'Access-Control-Allow-Headers': 'Content-Type, Authorization',
    };
}

// Main server function - adapt this to your platform
async function handleRequest(request) {
    const url = new URL(request.url);
    const method = request.method;
    const path = url.pathname;

    // Handle CORS preflight
    if (method === 'OPTIONS') {
        return new Response(null, {
            status: 200,
            headers: setCORSHeaders()
        });
    }

    try {
        switch (path) {
            case '/create-checkout-session':
                return await handleCreateCheckoutSession(request);
            case '/webhook':
                return await handleWebhook(request);
            case '/payment-status':
                return await handlePaymentStatus(request);
            default:
                return new Response('Not Found', { 
                    status: 404,
                    headers: setCORSHeaders()
                });
        }
    } catch (error) {
        console.error('Request error:', error);
        return new Response(JSON.stringify({ error: error.message }), {
            status: 500,
            headers: setCORSHeaders({
                'Content-Type': 'application/json'
            })
        });
    }
}

// Create Stripe Checkout Session
async function handleCreateCheckoutSession(request) {
    if (request.method !== 'POST') {
        return new Response('Method not allowed', { status: 405 });
    }

    try {
        const body = await request.json();
        const { product, customerEmail, quantity = 1 } = body;

        // Validate required fields
        if (!product || !product.name || !product.amount) {
            throw new Error('Missing required product information');
        }

        // Create checkout session
        const sessionData = {
            'payment_method_types[]': 'card',
            'line_items[0][price_data][currency]': product.currency || 'usd',
            'line_items[0][price_data][product_data][name]': product.name,
            'line_items[0][price_data][unit_amount]': product.amount.toString(),
            'line_items[0][quantity]': quantity.toString(),
            'mode': 'payment',
            'success_url': `${FRONTEND_URL}/success?session_id={CHECKOUT_SESSION_ID}`,
            'cancel_url': `${FRONTEND_URL}/cancel`,
        };

        // Add customer email if provided
        if (customerEmail) {
            sessionData['customer_email'] = customerEmail;
        }

        // Add product description if provided
        if (product.description) {
            sessionData['line_items[0][price_data][product_data][description]'] = product.description;
        }

        const session = await stripeRequest('checkout/sessions', sessionData);

        return new Response(JSON.stringify({ 
            sessionId: session.id,
            url: session.url 
        }), {
            status: 200,
            headers: setCORSHeaders({
                'Content-Type': 'application/json'
            })
        });

    } catch (error) {
        console.error('Checkout session error:', error);
        return new Response(JSON.stringify({ 
            error: error.message 
        }), {
            status: 400,
            headers: setCORSHeaders({
                'Content-Type': 'application/json'
            })
        });
    }
}

// Handle Stripe Webhooks
async function handleWebhook(request) {
    if (request.method !== 'POST') {
        return new Response('Method not allowed', { status: 405 });
    }

    try {
        const body = await request.text();
        const signature = request.headers.get('stripe-signature');

        if (!signature) {
            throw new Error('Missing Stripe signature');
        }

        // Verify webhook signature
        const event = await verifyWebhookSignature(body, signature);

        // Handle different event types
        switch (event.type) {
            case 'checkout.session.completed':
                await handleSuccessfulPayment(event.data.object);
                break;
            case 'payment_intent.succeeded':
                await handlePaymentSucceeded(event.data.object);
                break;
            case 'payment_intent.payment_failed':
                await handlePaymentFailed(event.data.object);
                break;
            default:
                console.log(`Unhandled event type: ${event.type}`);
        }

        return new Response(JSON.stringify({ received: true }), {
            status: 200,
            headers: { 'Content-Type': 'application/json' }
        });

    } catch (error) {
        console.error('Webhook error:', error);
        return new Response(JSON.stringify({ error: error.message }), {
            status: 400,
            headers: { 'Content-Type': 'application/json' }
        });
    }
}

// Verify webhook signature (simplified version)
async function verifyWebhookSignature(payload, signature) {
    // In production, implement proper signature verification
    // This is a simplified version - use Stripe's library for full security
    const elements = signature.split(',');
    const signatureHash = elements.find(el => el.startsWith('v1=')).split('=')[1];
    const timestamp = elements.find(el => el.startsWith('t=')).split('=')[1];

    // Create expected signature
    const encoder = new TextEncoder();
    const key = await crypto.subtle.importKey(
        'raw',
        encoder.encode(STRIPE_WEBHOOK_SECRET),
        { name: 'HMAC', hash: 'SHA-256' },
        false,
        ['sign']
    );

    const signedPayload = `${timestamp}.${payload}`;
    const expectedSignature = await crypto.subtle.sign(
        'HMAC',
        key,
        encoder.encode(signedPayload)
    );

    const expectedHash = Array.from(new Uint8Array(expectedSignature))
        .map(b => b.toString(16).padStart(2, '0'))
        .join('');

    if (expectedHash !== signatureHash) {
        throw new Error('Invalid signature');
    }

    return JSON.parse(payload);
}

// Handle successful payment
async function handleSuccessfulPayment(session) {
    console.log('Payment successful for session:', session.id);
    
    // Here you would typically:
    // 1. Update your database
    // 2. Send confirmation email
    // 3. Provision access to digital products
    // 4. Update inventory
    
    const customerEmail = session.customer_details?.email;
    const amount = session.amount_total;
    
    console.log(`Payment of ${amount/100} received from ${customerEmail}`);
    
    // Example: Save to your database
    // await savePaymentToDatabase({
    //     sessionId: session.id,
    //     email: customerEmail,
    //     amount: amount,
    //     status: 'completed'
    // });
}

// Handle payment succeeded
async function handlePaymentSucceeded(paymentIntent) {
    console.log('Payment intent succeeded:', paymentIntent.id);
    // Additional handling if needed
}

// Handle payment failed
async function handlePaymentFailed(paymentIntent) {
    console.log('Payment failed:', paymentIntent.id);
    // Handle failed payment logic
}

// Check payment status
async function handlePaymentStatus(request) {
    const url = new URL(request.url);
    const sessionId = url.searchParams.get('session_id');

    if (!sessionId) {
        return new Response(JSON.stringify({ error: 'Missing session_id' }), {
            status: 400,
            headers: setCORSHeaders({
                'Content-Type': 'application/json'
            })
        });
    }

    try {
        const session = await stripeRequest(`checkout/sessions/${sessionId}`, {}, 'GET');
        
        return new Response(JSON.stringify({
            status: session.payment_status,
            customerEmail: session.customer_details?.email,
            amount: session.amount_total
        }), {
            status: 200,
            headers: setCORSHeaders({
                'Content-Type': 'application/json'
            })
        });

    } catch (error) {
        return new Response(JSON.stringify({ error: error.message }), {
            status: 400,
            headers: setCORSHeaders({
                'Content-Type': 'application/json'
            })
        });
    }
}

// Platform-specific implementations:

// For Deno
// import { serve } from "https://deno.land/std@0.140.0/http/server.ts";
// serve(handleRequest);

// For Bun
// export default {
//   port: 3000,
//   fetch: handleRequest,
// };

// For Cloudflare Workers
// addEventListener('fetch', event => {
//   event.respondWith(handleRequest(event.request));
// });

// For Vercel
// export default handleRequest;

// For Netlify Functions
// exports.handler = async (event, context) => {
//   const request = new Request(event.url, {
//     method: event.httpMethod,
//     headers: event.headers,
//     body: event.body
//   });
//   const response = await handleRequest(request);
//   return {
//     statusCode: response.status,
//     headers: Object.fromEntries(response.headers.entries()),
//     body: await response.text()
//   };
// };

// Example usage function for testing
async function testServer() {
    console.log('Testing Stripe backend...');
    
    // Test create checkout session
    const testProduct = {
        name: 'Test Product',
        description: 'Test description',
        amount: 2000, // $20.00
        currency: 'usd'
    };

    const testRequest = new Request('https://localhost/create-checkout-session', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ product: testProduct })
    });

    try {
        const response = await handleRequest(testRequest);
        const result = await response.json();
        console.log('Test result:', result);
    } catch (error) {
        console.error('Test failed:', error);
    }
}

// Export the main handler and helper functions
export {
    handleRequest,
    handleCreateCheckoutSession,
    handleWebhook,
    handlePaymentStatus,
    testServer
};

// Update the fetch URL in your stripe.js file:
async function createCheckoutSession() {
    const response = await fetch('http://localhost:8000/create-checkout-session', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            product: product,
            customerEmail: null,
            quantity: 1
        })
    });
    // ... rest of the code
}