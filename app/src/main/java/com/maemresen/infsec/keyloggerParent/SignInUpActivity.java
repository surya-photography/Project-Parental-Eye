package com.maemresen.infsec.keyloggerParent;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


public class SignInUpActivity extends AppCompatActivity {
    
    private EditText emailEditText, passwordEditText;
    private Button signInButton, signUpButton, googleAuthButton;
    private TextView forgotPassword;
    private FirebaseAuth firebaseAuth;
    
    private static final int RC_SIGN_IN = 123;
    private GoogleSignInOptions googleSignInOptions;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    
    
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.signin_page );
        
        emailEditText = findViewById( R.id.email_edittext );
        passwordEditText = findViewById( R.id.password_edittext );
        signInButton = findViewById( R.id.custom_signin_button );
        signUpButton = findViewById( R.id.custom_signup_button );
        googleAuthButton = findViewById( R.id.google_login_button );
        forgotPassword = findViewById( R.id.forgot_password_button );
        
        firebaseAuth = FirebaseAuth.getInstance();
        
        
        signInButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                signIn();
            }
        } );
        
        signUpButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                signUp();
            }
        } );
        
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder( GoogleSignInOptions.DEFAULT_SIGN_IN )
                .requestIdToken( getString( R.string.default_web_client_id ) )
                .requestEmail()
                .build();
        
        googleSignInClient = GoogleSignIn.getClient( this, gso );
        
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult( ActivityResult result ) {
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent( data );
                            handleGoogleSignInResult( task );
                        } else {
                            Toast.makeText( getApplicationContext(), "Google Sign-In failed", Toast.LENGTH_SHORT ).show();
                        }
                    }
                } );
        
        googleAuthButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                googleSignInLauncher.launch( signInIntent );
            }
        } );
        
        forgotPassword.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                ForgotPassword();
            }
        } );
        
    }
    
    private void ForgotPassword() {
        
        String email = emailEditText.getText().toString().trim();
        
        if (TextUtils.isEmpty( email )) {
            emailEditText.setError( "Required" );
            return;
        }
        String gmailPattern = "[a-zA-Z0-9._%+-]+@gmail\\.com";
    
        if (!email.matches(gmailPattern)) {
            emailEditText.setError("Invalid Gmail format");
            return;
        }
        firebaseAuth.sendPasswordResetEmail( email )
                .addOnSuccessListener( new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess( Void aVoid ) {
                        // Password reset email sent successfully
                        Toast.makeText( SignInUpActivity.this, "Password reset email sent", Toast.LENGTH_SHORT ).show();
                    }
                } )
                .addOnFailureListener( new OnFailureListener() {
                    @Override
                    public void onFailure( @NonNull Exception e ) {
                        // Error occurred while sending the password reset email
                        Toast.makeText( SignInUpActivity.this, "Failed to send password reset email", Toast.LENGTH_SHORT ).show();
                    }
                } );
    }
    
    private void signIn() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        
        if (TextUtils.isEmpty( email )) {
            emailEditText.setError( "Required" );
            return;
        }
        String gmailPattern = "[a-zA-Z0-9._%+-]+@gmail\\.com";
    
        if (!email.matches(gmailPattern)) {
            emailEditText.setError("Invalid Gmail format");
            return;
        }
        if (TextUtils.isEmpty( password )) {
            passwordEditText.setError( "Required" );
            passwordEditText.setTextColor( Color.parseColor( "#000000" ) );
            return;
        }
        
        firebaseAuth.signInWithEmailAndPassword( email, password )
                .addOnCompleteListener( this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete( @NonNull Task<AuthResult> task ) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent( SignInUpActivity.this, MainActivity.class );
                            startActivity( intent );
                            finish();
                        } else {
                            passwordEditText.setTextColor( Color.parseColor( "#FF0000" ) );
                            Toast.makeText(getApplicationContext(), "Authentication failed!\nEmail Doesn't match Please Sign Up", Toast.LENGTH_LONG).show();
                        }
                    }
                } );
    }
    
    private void signUp() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        
        if (TextUtils.isEmpty( email )) {
            Toast.makeText( getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT ).show();
            return;
        }
        String gmailPattern = "[a-zA-Z0-9._%+-]+@gmail\\.com";
    
        if (!email.matches(gmailPattern)) {
            emailEditText.setError("Invalid Gmail format");
            return;
        }
        if (TextUtils.isEmpty( password )) {
            Toast.makeText( getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT ).show();
            return;
        }
        
        firebaseAuth.createUserWithEmailAndPassword( email, password )
                .addOnCompleteListener( this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete( @NonNull Task<AuthResult> task ) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent( SignInUpActivity.this, MainActivity.class );
                            startActivity( intent );
                            finish();
                        } else {
                            Toast.makeText( getApplicationContext(), "Registration failed!\nPlease try Again...",
                                    Toast.LENGTH_SHORT ).show();
                        }
                    }
                } );
    }
    
    private void handleGoogleSignInResult( Task<GoogleSignInAccount> completedTask ) {
        try {
            GoogleSignInAccount account = completedTask.getResult( ApiException.class );
            firebaseAuthWithGoogle( account );
        } catch (ApiException e) {
            Toast.makeText( getApplicationContext(), "Google Sign-In failed", Toast.LENGTH_SHORT ).show();
        }
    }
    
    private void firebaseAuthWithGoogle( GoogleSignInAccount account ) {
        AuthCredential credential = GoogleAuthProvider.getCredential( account.getIdToken(), null );
        firebaseAuth.signInWithCredential( credential )
                .addOnCompleteListener( this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete( @NonNull Task<AuthResult> task ) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            
                            // Proceed with your app logic
                            Intent intent = new Intent( SignInUpActivity.this, MainActivity.class );
                            startActivity( intent );
                            finish();
                        } else {
                            Toast.makeText( getApplicationContext(), "Firebase Authentication failed", Toast.LENGTH_SHORT ).show();
                        }
                    }
                } );
    }
}