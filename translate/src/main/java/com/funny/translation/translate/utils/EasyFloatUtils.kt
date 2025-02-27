package com.funny.translation.translate.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.*
import com.funny.translation.AppConfig
import com.funny.translation.Consts
import com.funny.translation.GlobalTranslationConfig
import com.funny.translation.helper.ClipBoardUtil
import com.funny.translation.helper.ScreenUtils
import com.funny.translation.helper.VibratorUtils
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.*
import com.funny.translation.translate.activity.StartCaptureScreenActivity
import com.funny.translation.translate.bean.TranslationConfig
import com.funny.translation.translate.engine.TextTranslationEngines
import com.funny.translation.translate.service.CaptureScreenService
import com.lzf.easyfloat.EasyFloat
import com.lzf.easyfloat.enums.ShowPattern
import com.lzf.easyfloat.enums.SidePattern
import com.lzf.easyfloat.interfaces.OnPermissionResult
import com.lzf.easyfloat.interfaces.OnTouchRangeListener
import com.lzf.easyfloat.permission.PermissionUtils
import com.lzf.easyfloat.utils.DragUtils
import com.lzf.easyfloat.widget.BaseSwitchView
import com.tomlonghurst.roundimageview.RoundImageView
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.min


object EasyFloatUtils {
    internal const val TAG_FLOAT_BALL = "ball"
    private const val TAG_TRANS_WINDOW = "window"

    private const val TAG = "EasyFloat"
    private var vibrating = false
    var initTransWindow = false
    var initFloatBall = false
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var translateConfigFlow =
        MutableStateFlow(TranslationConfig("", Language.AUTO, Language.CHINESE))
    private var translateJob: Job? = null

    private var inputTextFlow = MutableStateFlow("")

    fun initScreenSize() {
        AppConfig.SCREEN_WIDTH = ScreenUtils.getScreenWidth()
        AppConfig.SCREEN_HEIGHT = ScreenUtils.getScreenHeight()
    }
    
    private fun initTransWindow(view: View){
        view.layoutParams.width = (min(AppConfig.SCREEN_WIDTH, AppConfig.SCREEN_HEIGHT) * 0.9).toInt()

        val edittext = view.findViewById<EditText>(R.id.float_window_input)

        coroutineScope.launch {
            inputTextFlow.collect {
                withContext(Dispatchers.Main){
                    edittext.setText(it)
                    edittext.setSelection(it.length)
                }
            }
        }

        val spinnerSource: Spinner =
            view.findViewById<Spinner?>(R.id.float_window_spinner_source).apply {
                adapter = ArrayAdapter<String>(FunnyApplication.ctx, R.layout.view_spinner_text_item).apply {
                    addAll(enabledLanguages.value.map { it.displayText })
                    setDropDownViewResource(R.layout.view_spinner_dropdown_item)
                }
                setSelection(enabledLanguages.value.indexOf(translateConfigFlow.value.sourceLanguage ?: Language.AUTO))
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        translateConfigFlow.value = translateConfigFlow.value.copy(
                            sourceString = edittext.text.trim().toString(),
                            sourceLanguage = enabledLanguages.value[position]
                        )
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
//                        TODO("Not yet implemented")
                    }
                }
            }

        val spinnerTarget: Spinner =
            view.findViewById<Spinner?>(R.id.float_window_spinner_target).apply {
                adapter = ArrayAdapter<String>(FunnyApplication.ctx, R.layout.view_spinner_text_item).apply {
                    addAll(enabledLanguages.value.map { it.displayText })
                    setDropDownViewResource(R.layout.view_spinner_dropdown_item)
                }
                setSelection(enabledLanguages.value.indexOf(translateConfigFlow.value.targetLanguage ?: Language.CHINESE))
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        translateConfigFlow.value = translateConfigFlow.value.copy(
                            sourceString = edittext.text.trim().toString(),
                            targetLanguage = enabledLanguages.value[position]
                        )
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
//                        TODO("Not yet implemented")
                    }
                }
            }

        coroutineScope.launch {
            enabledLanguages.collect {
                withContext(Dispatchers.Main){
                    spinnerSource.adapter = ArrayAdapter<String>(FunnyApplication.ctx, R.layout.view_spinner_text_item).apply {
                        addAll(enabledLanguages.value.map { it.displayText })
                        setDropDownViewResource(R.layout.view_spinner_dropdown_item)
                    }
                    spinnerSource.setSelection(enabledLanguages.value.indexOf(translateConfigFlow.value.sourceLanguage ?: Language.AUTO))

                    spinnerTarget.adapter = ArrayAdapter<String>(FunnyApplication.ctx, R.layout.view_spinner_text_item).apply {
                        addAll(enabledLanguages.value.map { it.displayText })
                        setDropDownViewResource(R.layout.view_spinner_dropdown_item)
                    }
                    spinnerTarget.setSelection(enabledLanguages.value.indexOf(translateConfigFlow.value.targetLanguage ?: Language.CHINESE))
                }
            }
        }

        val rotateAnimation = RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 300
        }

        view.findViewById<ImageButton?>(R.id.float_window_exchange_button).apply {
            setOnClickListener {
                val temp = spinnerSource.selectedItemPosition
                spinnerSource.setSelection(spinnerTarget.selectedItemPosition, true)
                spinnerTarget.setSelection(temp, true)
                startAnimation(rotateAnimation)
            }
        }

        val resultText: TextView = view.findViewById(R.id.float_window_text)
        val speakBtn = view.findViewById<ImageButton>(R.id.float_window_speak_btn).apply {
            setOnClickListener {
                val txt = resultText.text
                if (txt.isNotEmpty()){
                    AudioPlayer.playOrPause(txt.toString(), findLanguageById(spinnerTarget.selectedItemPosition)){
                        context.toastOnUi("朗读错误")
                    }
                }
            }
        }
        val copyBtn = view.findViewById<ImageButton>(R.id.float_window_copy_btn).apply {
            setOnClickListener {
                val txt = resultText.text
                if (txt.isNotEmpty()){
                    ClipBoardUtil.copy(context, txt)
                    context.toastOnUi("已复制到剪贴板")
                }
            }
        }

        view.findViewById<ImageButton?>(R.id.float_window_close).apply {
            setOnClickListener {
                EasyFloat.hide(TAG_TRANS_WINDOW)
            }
        }

        translateJob = coroutineScope.launch(Dispatchers.IO) {
            translateConfigFlow.collect {
                kotlin.runCatching {
                    if (it.sourceString!=null && it.sourceString!="") {
                        val sourceLanguage = enabledLanguages.value[spinnerSource.selectedItemPosition]
                        val targetLanguage = enabledLanguages.value[spinnerTarget.selectedItemPosition]
                        val task = TranslateUtils.createTask(
                            TextTranslationEngines.BaiduNormal,
                            it.sourceString!!,
                            sourceLanguage,
                            targetLanguage
                        )

                        // 设置全局的翻译参数
                        with(GlobalTranslationConfig){
                            this.sourceLanguage = task.sourceLanguage
                            this.targetLanguage = task.targetLanguage
                            this.sourceString   = task.sourceString
                        }

                        withContext(Dispatchers.Main) {
                            resultText.text = "正在翻译……"
                        }
                        task.translate()
                        withContext(Dispatchers.Main) {
                            resultText.text = task.result.basicResult.trans
                            if (speakBtn.visibility != View.VISIBLE){
                                speakBtn.visibility = View.VISIBLE
                            }
                            if (copyBtn.visibility != View.VISIBLE){
                                copyBtn.visibility = View.VISIBLE
                            }
                        }
                    }
                }.onFailure {
                    withContext(Dispatchers.Main) {
                        it.printStackTrace()
                        resultText.text = FunnyApplication.ctx.resources.getString(R.string.trans_error).format(it)
                    }
                }
            }
        }

        view.findViewById<TextView?>(R.id.float_window_translate).apply {
            setOnClickListener {
                val inputText = edittext.text.trim()
                if (inputText.isNotEmpty()) {
                    translateConfigFlow.value =
                        translateConfigFlow.value.copy(sourceString = inputText.toString())
                }
            }
        }

        view.findViewById<ImageButton>(R.id.float_window_open_app_btn).apply {
            setOnClickListener {
                Intent().apply {
                    action = Intent.ACTION_VIEW
                    val text = Uri.encode(edittext.text.trim().toString())
                    data = Uri.parse("funny://translation/translate?text=$text&sourceId=${spinnerSource.selectedItemPosition}&targetId=${spinnerTarget.selectedItemPosition}")
                    flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(Consts.EXTRA_OPEN_IN_APP, true)
                }.let {
                    context.startActivity(it)
                }
            }
        }
    }

     fun showTransWindow(){
        if(!initTransWindow){
            EasyFloat.with(FunnyApplication.ctx)
                .setTag(TAG_TRANS_WINDOW)
                .setLayout(R.layout.layout_float_window){ view ->
                    initTransWindow(view)
                }
                .hasEditText(true)
                .setShowPattern(ShowPattern.ALL_TIME)
                .setSidePattern(SidePattern.DEFAULT)
                .setImmersionStatusBar(true)
                .setGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP, 0, 100)
                .show()
            initTransWindow = true
        }else{
            EasyFloat.show(TAG_TRANS_WINDOW)
        }
    }

    fun resetFloatBallPlace(){
        initScreenSize()
        EasyFloat.updateFloat(TAG_FLOAT_BALL, AppConfig.SCREEN_WIDTH - 200, AppConfig.SCREEN_HEIGHT * 2 / 3)
    }

    fun startTranslate(sourceString: String, sourceLanguage: Language, targetLanguage: Language){
        inputTextFlow.value = sourceString
        translateConfigFlow.value = translateConfigFlow.value.copy(sourceString, sourceLanguage, targetLanguage)
    }

    @SuppressLint("MissingPermission")
    fun setVibrator(inRange: Boolean) {
        val vibrator = VibratorUtils.vibrator
        if (!vibrator.hasVibrator() || (inRange && vibrating)) return
        vibrating = inRange
        if (inRange) VibratorUtils.vibrate(100)
        else vibrator.cancel()
    }

    private fun _showFloatBall(){
        if(initFloatBall){
            EasyFloat.show(TAG_FLOAT_BALL)
        }else {
            var plusView: View? = null
            EasyFloat.with(FunnyApplication.ctx)
                .setTag(TAG_FLOAT_BALL)
                .setLayout(R.layout.layout_float_ball) { view ->
                    view.findViewById<RoundImageView>(R.id.float_ball_image).apply {
                        setOnClickListener {
                            showTransWindow()
                        }
                        setOnLongClickListener {
                            VibratorUtils.vibrate()
                            if (!CaptureScreenService.hasMediaProjection) {
                                // 如果没有权限，则跳转到申请权限的界面
                                StartCaptureScreenActivity.start(null)
                            } else {
                                StartCaptureScreenActivity.start(CaptureScreenService.WHOLE_SCREEN_RECT)
                            }
                            true
                        }
                    }
                    plusView = view.findViewById<ImageView>(R.id.float_ball_plus).apply {
                        alpha = 0.5f
                    }
                }
                .setShowPattern(ShowPattern.ALL_TIME)
                .setSidePattern(SidePattern.RESULT_HORIZONTAL)
                .setImmersionStatusBar(true)
                .setGravity(Gravity.END or Gravity.BOTTOM, -20, -200)
                .registerCallback {
                    drag { view, motionEvent ->
                        FloatScreenCaptureUtils.registerDrag(
                            plusView = plusView, motionEvent = motionEvent
                        )
                        // 截屏的时候就不判定删除了
                        if (FloatScreenCaptureUtils.whetherInScreenCaptureMode) return@drag
                        DragUtils.registerDragClose(motionEvent, object : OnTouchRangeListener {
                            override fun touchInRange(inRange: Boolean, view: BaseSwitchView) {
                                setVibrator(inRange)
                                view.findViewById<TextView>(com.lzf.easyfloat.R.id.tv_delete).text =
                                    if (inRange) "松手删除" else "删除浮窗"

                                view.findViewById<ImageView>(com.lzf.easyfloat.R.id.iv_delete)
                                    .setImageResource(
                                        if (inRange) com.lzf.easyfloat.R.drawable.icon_delete_selected
                                        else com.lzf.easyfloat.R.drawable.icon_delete_normal
                                    )
                            }

                            override fun touchUpInRange() {
                                EasyFloat.dismiss(TAG_FLOAT_BALL)
                                AppConfig.sShowFloatWindow.value = false
                                initFloatBall = false
                            }
                        }, showPattern = ShowPattern.ALL_TIME)
                    }
                    dragEnd {
                        FloatScreenCaptureUtils.registerDragEnd(plusView)
                    }
                }
                .show()
            initFloatBall = true
        }
    }


    fun showFloatBall(activity : Activity){
        if(!PermissionUtils.checkPermission(FunnyApplication.ctx)) {
            AlertDialog.Builder(activity)
                .setMessage("使用浮窗功能，需要您授权悬浮窗权限。")
                .setPositiveButton("去开启") { _, _ ->
                    PermissionUtils.requestPermission(activity, object : OnPermissionResult {
                        override fun permissionResult(isOpen: Boolean) {
                            showFloatBall(activity)
                        }
                    })
                }
                .setNegativeButton("取消") { _, _ -> }
                .show()
        }else{
            _showFloatBall()
        }
    }

    fun hideAllFloatWindow(){
        EasyFloat.hide(TAG_TRANS_WINDOW)
        EasyFloat.hide(TAG_FLOAT_BALL)
    }

    fun dismissAll(){
        EasyFloat.dismiss(TAG_TRANS_WINDOW)
        EasyFloat.dismiss(TAG_FLOAT_BALL)
        FloatScreenCaptureUtils.dismiss()
        translateJob?.cancel()
    }

    fun isShowingFloatBall() = EasyFloat.isShow(TAG_FLOAT_BALL)
}