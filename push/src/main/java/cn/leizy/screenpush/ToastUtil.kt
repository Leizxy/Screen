package cn.leizy.screenpush

import android.app.Activity
import android.content.Context
import android.widget.Toast

/**
 * @author Created by wulei
 * @date 2021/3/15, 015
 * @description
 */
object ToastUtil {
    fun show(context: Activity, string: String) {
        context.runOnUiThread {
            Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
        }
    }
}