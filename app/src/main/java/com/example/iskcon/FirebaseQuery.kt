package com.example.iskcon

import android.content.ContentValues.TAG
import android.util.ArrayMap
import android.util.Log
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

var preacher: PreacherModel = PreacherModel("NA", "NA", "NA", "NA","0")
object FirebaseQuery {
    var firestore: FirebaseFirestore? = null

    fun createStudent(name:String,email:String,number:String,address:String,college:String,dob:String,insta:String,education:String,occupation:String,completeListener: MyCompleteListener){
        val devoteeData: MutableMap<String, Any> = ArrayMap()
        devoteeData["NAME"] = name
        devoteeData["EMAIL-ID"]=email
        devoteeData["PHONE"] = number
        devoteeData["ADDRESS"] = address
        devoteeData["COLLEGE"] = college
        devoteeData["DOB"] = dob
        devoteeData["INSTA-ID"] = insta
        devoteeData["EDUCATION"] = education
        devoteeData["OCCUPATION"] = occupation

        val userDoc: DocumentReference? =
            firestore?.collection("STUDENTS")?.document(
                name
            )

        val batch: WriteBatch? = firestore?.batch()
        batch?.set(userDoc!!, devoteeData)

        val countDoc: DocumentReference? =
            firestore?.collection("STUDENTS")
                ?.document("TOTAL_STUDENTS")

        if (countDoc != null) {
            batch?.update(countDoc, "COUNT", FieldValue.increment(1) )
        }
        batch?.commit()?.addOnSuccessListener {
            completeListener.onSuccess()

        }?.addOnFailureListener { completeListener.onFailure() }
    }
    fun addStudentToPreacher(name:String,email:String,number:String,address:String,college:String,dob:String,insta:String,education:String,occupation:String,completeListener: MyCompleteListener){
        val userDoc =
            FirebaseAuth.getInstance().uid?.let {
                firestore?.collection("PREACHERS")?.document(it)
            }
        userDoc?.collection("STUDENTS")?.document()
            ?.set(Student(name,email,number,address,college,dob,insta,education,occupation))
            ?.addOnCompleteListener {
                if (it.isSuccessful) {
                    // Increment the field value by 1
                    val increment = FieldValue.increment(1)

                    // Update the field value
                    userDoc.update("STUDENTS_ENROLLED", increment)
                        .addOnSuccessListener {
                            // Field value incremented successfully
                        }
                        .addOnFailureListener { e ->
                            // Handle any errors that occurred while incrementing the field value
                        }

                    Log.d(TAG, "Data added to Firestore ${it.result}")
                } else {
                    Log.d(TAG, "Data added to Firestore ${it.exception}")
                }
            }
    }

    fun getPreacherData(completeListener: MyCompleteListener) {
        FirebaseAuth.getInstance().uid?.let {
            firestore?.collection("PREACHERS")
                ?.document(it)
                ?.get()
                ?.addOnSuccessListener(OnSuccessListener<DocumentSnapshot> { documentSnapshot: DocumentSnapshot ->
                    preacher.name=documentSnapshot.getString("NAME").toString()
                    preacher.email=documentSnapshot.getString("EMAIL-ID").toString()
                    preacher.phone=documentSnapshot.getString("PHONE").toString()
                    preacher.instaId=documentSnapshot.getString("INSTA").toString()
                    preacher.studentEnrolled=documentSnapshot.getString("STUDENTS_ENROLLED").toString()
                    completeListener.onSuccess()
                })
                ?.addOnFailureListener(OnFailureListener { e: Exception? -> completeListener.onFailure() })
        }
    }


}