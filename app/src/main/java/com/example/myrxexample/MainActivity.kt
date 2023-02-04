package com.example.myrxexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.button)

        simpleObserver()

        button.clicks()
            .throttleFirst(2000, TimeUnit.MILLISECONDS)
            .subscribe {
                Log.d("My Rx", "Button Clicked")
            }

        networkCall()
    }

    private fun networkCall() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://fakestoreapi.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()

        val productService = retrofit.create(ProductService::class.java)
        productService.getProducts()
            .subscribeOn(Schedulers.io())
                // ^ this will work upstream, so network call will work on io thread

                // this will work downstream, so UI will work on main thread
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                // Log.d("My Rx", it.toString())

                val titleList = mutableListOf<String>()

                if (it.isNotEmpty()) {

                    it.forEach {
                        titleList.add(it.title)
                    }
                } else {
                    Log.d("My Rx", "No Data")
                }

                Log.d("My Rx", titleList.toString())
            }
    }

    private fun simpleObserver() {
        val list = listOf<String>("Google", "Meta", "IBM", "Microsoft")
        val observable = Observable.fromIterable(list)

        observable.subscribe(object : Observer<String> {
            override fun onSubscribe(d: Disposable) {
                Log.d("My Rx", "onSubscribe")
            }

            override fun onError(e: Throwable) {
                Log.d("My Rx", "onError - ${e.message}")
            }

            override fun onComplete() {
                Log.d("My Rx", "onComplete")
            }

            override fun onNext(t: String) {
                Log.d("My Rx", "onNext - $t")
            }
        })
    }
}
