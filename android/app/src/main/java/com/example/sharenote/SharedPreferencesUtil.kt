package com.example.sharenote

import android.content.Context

object SharedPreferencesUtil {
    private const val PREF_NAME = "MyPrefs"
    private const val KEY_RECENT_WORKSPACE_ID = "recent_workspace_id"

    private const val PREF_NAME1 = "MyPrefs1"
    private const val KEY_RECENT_NOTE_ID = "recent_note_id"

    // 유저 정보 저장
    private const val PREF_NAME_USER = "MyPrefs_user"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_EMAIL = "user_email"

    // 최근 워크스페이스 ID 저장
    fun saveRecentWorkspaceId(context: Context, workspaceId: String) {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(KEY_RECENT_WORKSPACE_ID, workspaceId).apply()
    }

    // 최근 워크스페이스 ID 불러오기
    fun getRecentWorkspaceId(context: Context): String? {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_RECENT_WORKSPACE_ID, null)
    }

    // 최근 노트 ID 저장
    fun saveRecentNoteId(context: Context, noteId: String) {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME1, Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(KEY_RECENT_NOTE_ID, noteId).apply()
    }

    // 최근 노트 ID 불러오기
    fun getRecentNoteId(context: Context): String? {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME1, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_RECENT_NOTE_ID, null)
    }

    // 사용자 정보 저장
    fun saveUserData(context: Context, name: String, userId: String, email: String) {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME_USER, Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }

    // 사용자 이름 불러오기
    fun getUserName(context: Context): String? {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME_USER, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_USER_NAME, null)
    }

    // 사용자 ID 불러오기
    fun getUserId(context: Context): String? {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME_USER, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_USER_ID, null)
    }

    // 사용자 이메일 불러오기
    fun getUserEmail(context: Context): String? {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME_USER, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_USER_EMAIL, null)
    }

}