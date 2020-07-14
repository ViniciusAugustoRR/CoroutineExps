package com.example.coroutineexps

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main



class MainActivity : AppCompatActivity() {
    private val JOB_TIME = 3000
    private lateinit var jobPrintNome: CompletableJob

    //STATUS POSSIVELS
    private val STARTJOB = "START JOB"
    private val CANCELJOB = "CANCEL JOB"

    private val CONCLUIDO = "CONCLUIDO"
    private val EM_PROGRESSO = "EM PROGRESSO"
    private val CANCELADO = "CANCELADO"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        start_job_button.text = STARTJOB

        start_job_button.setOnClickListener {
            if (!::jobPrintNome.isInitialized) {
                initJob()
            }
            startJobOrCancel()

        }

    }


    fun startJobOrCancel(){

        //CASO O JOB AINDA NÃO TENHA SIDO INICIADO
        if(start_job_button.text == STARTJOB) {
            showToast("JOB FOI INICIADO")
            start_job_button.text = CANCELJOB


            CoroutineScope(Default + jobPrintNome).launch {
                updateTV_JOB(EM_PROGRESSO)

                //INICIE O PROCESSO QUE DEMANDARA TEMPO
                delay(JOB_TIME.toLong())

                //ATUALIZE A VIEW QUANDO O PROCESSO ANTERIOR FOR CONCLUIDO
                updateTV_JOB(CONCLUIDO)
            }


        //CASO O JOB TENHA INICIADO E O USUARIO DESEJE CANCELA-LO
        } else {
            println("Canceling Job ...")
            resetJob()
            updateTV_JOB(CANCELADO)
        }
    }

    private fun updateTV_JOB(text: String){
        GlobalScope.launch(Main) {
            job_status.text = text

            if(text == EM_PROGRESSO){
                loading_panel.visibility = View.VISIBLE
            }

            if(text == CONCLUIDO){
                delay(1000)
                loading_panel.visibility = View.INVISIBLE
                actv_tv_name.visibility = View.VISIBLE

                start_job_button.text = STARTJOB
                job_status.text = CONCLUIDO

                showToast("JOB FOI CONCLUIDO")
            }

            if(text == CANCELADO){
                loading_panel.visibility = View.INVISIBLE
                showToast("JOB FOI CANCELADO")
            }

        }
    }

    fun initJob(){
        job_status.text = EM_PROGRESSO

        jobPrintNome = Job()
        jobPrintNome.invokeOnCompletion {
            it?.message.let {
                var msg = it
                if(!msg.isNullOrBlank()){
                    msg = "ERRO DE CANCELAMENTO DESCONHECIDO"
                }
                println("$jobPrintNome foi cancelado. Motivo: $msg")

                showToast(msg)
            }
        }
    }

    fun resetJob(){
        if(jobPrintNome.isActive || jobPrintNome.isCompleted){
            jobPrintNome.cancel(CancellationException("RESETANDO O JOB"))
        }

        initJob()
    }

    fun showToast(text: String?){
        GlobalScope.launch(Main){
            Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT).show()
        }
    }






}
