package com.example.muhasabe_mobil_fuzzyox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DBHandler {

    /*
        //Bu sınıfın yazılma amacı,Database bağlantılarının tek bir sınıf üzerinden yapılmasıdır.
        //Bu sınıfın metodları ile DB üzerinde işlemler yapılmaktadır.
        //Lütfen DB işlemlerini bu sınıfı kullanarak yapınız.
     */

    private  FirebaseAuth mAuth;
    private  FirebaseUser mUser;
    private  FirebaseFirestore firestore;

    public DBHandler(FirebaseAuth mAuth, FirebaseUser mUser, FirebaseFirestore firestore) {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        this.mAuth = mAuth;
        this.mUser = mUser;
        this.firestore = firestore;
    }

    public DBHandler(FirebaseAuth mAuth){
        mAuth = FirebaseAuth.getInstance();
        this.mAuth = mAuth;
    }

    public DBHandler(FirebaseAuth mAuth, FirebaseFirestore firestore) {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        this.mAuth = mAuth;
        this.firestore = firestore;
    }

    public FirebaseAuth getmAuth() {
        return mAuth;
    }

    public void setmAuth(FirebaseAuth mAuth) {
        this.mAuth = mAuth;
    }

    public FirebaseUser getmUser() {
        return mUser;
    }

    public void setmUser(FirebaseUser mUser) {
        this.mUser = mUser;
    }

    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    public void setFirestore(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public void addRegister(User user, RegisterActivity my_registerActivity){

        getmAuth().createUserWithEmailAndPassword(user.getE_mail(), user.getPass())
                .addOnCompleteListener(my_registerActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            setmUser(getmAuth().getCurrentUser());

                            getFirestore().collection("Kullanıcılar").document(getmUser().getUid())
                                    .set(user).addOnCompleteListener(my_registerActivity, new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                        Toast.makeText(my_registerActivity, "Kayıt Başarılı.", Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(my_registerActivity, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else
                            Toast.makeText(my_registerActivity, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }
    public void addLogin(String email, String pass, LoginActivity loginActivity){

        getmAuth().signInWithEmailAndPassword(email , pass)
                .addOnSuccessListener(loginActivity, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        /*
                        E-mail password update işlemi Authentication sekmesinde yapıldığı için firestore üzerinde güncel görünmüyordu.
                        UpdatePass methodu ile bu hata giderildi.
                         */
                        updatePass(pass, loginActivity, mAuth.getCurrentUser().getUid());

                        Toast.makeText(loginActivity, "Giriş Başarılı!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(loginActivity, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(loginActivity, "E-Posta veya Şifre Yanlış!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void resetPass(String email, Activity activity){
        getmAuth().sendPasswordResetEmail(email)
                .addOnCompleteListener(activity , new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            AlertDialog.Builder alert = new AlertDialog.Builder(activity);
                            alert.setTitle("Şifre Sıfırlama İsteği");
                            alert.setMessage("Şifre Sıfırlama Bağlantısı Mail Hesabınıza Gönderildi.");
                            alert.setCancelable(false);
                            alert.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(activity , LoginActivity.class);
                                    activity.startActivity(intent);
                                }
                            });
                            alert.show();
                        }
                        else
                            Toast.makeText(activity, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void updatePass(String pass, Activity activity , String uId){
        firestore.collection("Kullanıcılar")
                .document(uId).update("pass" ,pass);
    }

    public void DeleteUserFirestore(Activity activity , String uId){
        firestore.collection("Kullanıcılar")
                .document(uId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                    Toast.makeText(activity, "Firestore Silme İşlemi Başarılı", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(activity, "Firestore Silme İşlemi başarısız!!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void DeleteUserAuthentication(Activity activity , String uId){
        mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                    Toast.makeText(activity, "Kişilerden Silme İşlemi Başarılı!", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(activity, "Kişilerden Silme İşlemi Başarısız!!", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
