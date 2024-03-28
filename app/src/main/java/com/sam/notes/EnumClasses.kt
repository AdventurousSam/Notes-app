package com.sam.notes

enum class NotesScreen {
    Login,
    Main,
    CS304,
    PdfView
}

enum class Authentication {
    ConfirmPasswordMissMatch,
    InvalidEmail,
    InvalidPass,
    Failed,
    Successful
}