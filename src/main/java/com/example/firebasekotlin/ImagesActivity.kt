package com.example.firebasekotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_images.*

class ImagesActivity : AppCompatActivity(), ImageAdapter.OnItemClickListener {

    lateinit var mDatabaseRef: DatabaseReference
    lateinit var mStorage: FirebaseStorage
    lateinit var mUploads: ArrayList<Upload>
    lateinit var mDBListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_images)



        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.setHasFixedSize(true)

        mUploads = ArrayList()


        val mAdapter = ImageAdapter(this@ImagesActivity, mUploads)
        recycler_view.adapter = mAdapter
        mAdapter.setOnItemClickListener(this)

        mStorage = FirebaseStorage.getInstance()

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads")


        mDBListener = mDatabaseRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {

                mUploads.clear()
                for (postSnapshot in p0.children) {

                    val upload = postSnapshot.getValue(Upload::class.java)
                    upload?.mKey = postSnapshot.key!!

                    upload?.let {
                        mUploads.add(it)
                    }

                }
                mAdapter.notifyDataSetChanged()
                progress_circle.visibility = View.INVISIBLE

            }

            override fun onCancelled(p0: DatabaseError) {

                showToast(p0.message, Toast.LENGTH_LONG)
                progress_circle.visibility = View.INVISIBLE

            }


        })


    }

    override fun OnItemClick(pos: Int) {

        showToast("Normal click at position $pos")
    }

    override fun OnDoWhateverClicked(pos: Int) {
        showToast("Whatever click at position $pos")

    }

    override fun OnDeleteClicked(pos: Int) {

        val selectedItem = mUploads[pos]
        val selectedKey: String = selectedItem.mKey

        var imageRef: StorageReference = mStorage.getReferenceFromUrl(selectedItem.mImageUrl)
        imageRef.delete()
            .addOnSuccessListener {
                mDatabaseRef.child(selectedKey).removeValue()
                showToast("Item Deleted")

            }.addOnFailureListener {

                showToast("Cannot Delete Item")
            }

    }

    override fun onDestroy() {
        super.onDestroy()

        mDatabaseRef.removeEventListener(mDBListener)
    }

}
