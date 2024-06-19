package com.example.sharenote

import android.content.Context

object SharedPreferencesUtil {
    private const val PREF_NAME = "MyPrefs"
    private const val KEY_RECENT_WORKSPACE_ID = "recent_workspace_id"

    private const val KEY_RECENT_WORKSPACE_NAME = "recent_workspace_name"

    private const val PREF_NAME1 = "MyPrefs1"
    private const val KEY_RECENT_NOTE_ID = "recent_note_id"
    private const val RECENT_NOTE_IDS = "recent_note_ids"

    private const val KEY_RECENT_NOTE_TITLE = "recent_note_title"

    private const val KEY_RECENT_QUIZ_ID = "recent_quiz_id"

    private const val KEY_RECENT_PAGE_ID = "recent_page_id"


    // 유저 정보 저장
    private const val PREF_NAME_USER = "MyPrefs_user"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_EMAIL = "user_email"

    private const val KEY_FCM_TOKEN = "fcm_token"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"

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

    // 최근 워크스페이스 이름 저장
    fun saveRecentWorkspaceName(context: Context, workspaceName: String) {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(KEY_RECENT_WORKSPACE_NAME, workspaceName).apply()
    }

    // 최근 워크스페이스 이름 불러오기
    fun getRecentWorkspaceName(context: Context): String? {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_RECENT_WORKSPACE_NAME, null)
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

    fun saveRecentNoteIds(context: Context, noteId: String) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME1, Context.MODE_PRIVATE)
        val recentNotes = getRecentNoteIds(context).toMutableList()

        // 이미 있는 경우 기존 위치에서 제거
        if (recentNotes.contains(noteId)) {
            recentNotes.remove(noteId)
        }

        // 새로운 노트 ID를 리스트의 맨 앞에 추가
        recentNotes.add(0, noteId)

        // 리스트를 최대 6개까지 유지
        if (recentNotes.size > 6) {
            recentNotes.removeAt(recentNotes.size - 1)
        }

        // 수정된 리스트를 SharedPreferences에 저장
        val editor = sharedPreferences.edit()
        editor.putStringSet(RECENT_NOTE_IDS, recentNotes.toSet())
        editor.apply()
    }


    fun getRecentNoteIds(context: Context): List<String> {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME1, Context.MODE_PRIVATE)
        return sharedPreferences.getStringSet(RECENT_NOTE_IDS, emptySet())?.toList() ?: emptyList()
    }

    fun saveRecentNoteTitle(context: Context, noteTitle: String) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(KEY_RECENT_NOTE_TITLE, noteTitle).apply()
    }

    fun getRecentNoteTitle(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_RECENT_NOTE_TITLE, null)
    }

    fun saveRecentQuizId(context: Context, quizId: String) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(KEY_RECENT_QUIZ_ID, quizId).apply()
    }

    fun getRecentQuizId(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_RECENT_QUIZ_ID, null)
    }

    // 최근 페이지 ID 저장
    fun saveRecentPageId(context: Context, pageId: String) {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME1, Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(KEY_RECENT_PAGE_ID, pageId).apply()
    }

    // 최근 페이지 ID 불러오기
    fun getRecentPageId(context: Context): String? {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME1, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_RECENT_PAGE_ID, null)
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

    // FCM 토큰 저장
    fun saveFcmToken(context: Context, token: String) {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME_USER, Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(KEY_FCM_TOKEN, token).apply()
    }

    // FCM 토큰 불러오기
    fun getFcmToken(context: Context): String? {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME_USER, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_FCM_TOKEN, null)
    }

    // Access Token 저장
    fun saveAccessToken(context: Context, accessToken: String) {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME_USER, Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(KEY_ACCESS_TOKEN, accessToken).apply()
    }

    // Access Token 불러오기
    fun getAccessToken(context: Context): String? {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME_USER, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_ACCESS_TOKEN, null)
    }

    // Refresh Token 저장
    fun saveRefreshToken(context: Context, refreshToken: String) {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME_USER, Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(KEY_REFRESH_TOKEN, refreshToken).apply()
    }

    // Refresh Token 불러오기
    fun getRefreshToken(context: Context): String? {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME_USER, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_REFRESH_TOKEN, null)
    }

}