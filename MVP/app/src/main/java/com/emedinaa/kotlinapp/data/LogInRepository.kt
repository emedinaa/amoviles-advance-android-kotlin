package com.emedinaa.kotlinapp.data

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.emedinaa.kotlinapp.model.User
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject

class LogInRepository(val context:Context) {

    val TAG = "LoginRequestTAG"

    private val requestQueue=Volley.newRequestQueue(context)
    private val url= EndPoints.logIn()
    private val gson= Gson()

    fun logInDR( username:String,  password:String,result:(data:DataResult<User?>)->Unit){
        val jsonObject = JSONObject()
        jsonObject.put("username",username)
        jsonObject.put("password",password)

        val jsonObjectRequest= JsonObjectRequest(Request.Method.POST,url,jsonObject,
            Response.Listener { response ->
                Log.v("CONSOLE",response.toString())
                var jsonObject:JSONObject?
                try {
                    jsonObject=JSONObject(response.toString())
                    val loginResponse: LogInResponse = gson.fromJson(
                        jsonObject.toString(),
                        LogInResponse::class.java
                    )
                    Log.v("CONSOLE","LogInResponse $loginResponse")
                    when {
                        loginResponse.isSuccess() -> result(DataResult.Success(loginResponse.data))
                        else -> result(DataResult.Failure(
                            Exception(loginResponse.msg)))
                    }

                }catch (e:JSONException){
                    result(DataResult.Failure(e))
                }
            },
            Response.ErrorListener { error ->
                Log.v("CONSOLE","Error message ${error.message} responseError ${error.networkResponse?.statusCode}")
                val messageError= "error : ${error.networkResponse?.statusCode} ".plus("message ${error.message}")

                if(error.networkResponse?.statusCode==404){
                    result(DataResult.Failure(Exception("Usuario o password incorrectos")))
                }else{
                    result(DataResult.Failure(Exception(messageError)))
                }
            }
        )
        requestQueue.add(jsonObjectRequest)
    }

    fun logIn( username:String,  password:String,callback: OperationCallback){
        val jsonObject = JSONObject()
        jsonObject.put("username",username)
        jsonObject.put("password",password)

        val jsonObjectRequest= JsonObjectRequest(Request.Method.POST,url,jsonObject,
            Response.Listener { response ->
                Log.v("CONSOLE",response.toString())
                var jsonObject:JSONObject?
                try {
                    jsonObject=JSONObject(response.toString())
                    val loginResponse: LogInResponse = gson.fromJson(
                        jsonObject.toString(),
                        LogInResponse::class.java
                    )
                    if(loginResponse.isSuccess()){
                        callback.onSuccess(loginResponse.data)
                    }else{
                        callback.onError(loginResponse.msg)
                    }

                }catch (e:JSONException){
                    callback.onError(e.message)
                }
            },
            Response.ErrorListener { error ->
                Log.v("CONSOLE","message ${error.message} responseError ${error.networkResponse?.statusCode}")
                val messageError= "error : ${error.networkResponse?.statusCode} ".plus("message ${error.message}")
                callback.onError(messageError)
            }
        )
        requestQueue.add(jsonObjectRequest)
    }

    fun cancelOperation(){
        requestQueue?.cancelAll(TAG)
    }

    companion object {

        private var INSTANCE: LogInRepository? = null

        /**
         * Returns the single instance of this class, creating it if necessary.
         * @param tasksRemoteDataSource the backend data source
         * *
         * @param tasksLocalDataSource  the device storage data source
         * *
         * @return the [TasksRepository] instance
         */
        @JvmStatic fun getInstance(context: Context): LogInRepository {
            return INSTANCE ?: LogInRepository(context)
                .apply { INSTANCE = this }
        }

        /**
         * Used to force [getInstance] to create a new instance
         * next time it's called.
         */
        @JvmStatic fun destroyInstance() {
            INSTANCE = null
        }
    }
}