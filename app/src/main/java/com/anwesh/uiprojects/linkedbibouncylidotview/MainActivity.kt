package com.anwesh.uiprojects.linkedbibouncylidotview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.bibouncylidotview.BiBouncyLiDotView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BiBouncyLiDotView.create(this)
    }
}
