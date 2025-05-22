package com.a303.helpmet.presentation.model

enum class VoiceCommand(val korLabel: String, val keywords: List<String>){
    TURN_LEFT("좌회전", listOf("좌회전", "왼쪽")),
    TURN_RIGHT("우회전", listOf("우회전", "오른쪽")),
    END_TURN_SIGNAL("깜빡이 종료", listOf("깜빡이 종료", "깜빡이 꺼", "지시등 종료", "지시등 꺼", "꺼")),
    RESTROOM("화장실", listOf("화장실", "장실")),
    PARKING_ZONE("대여소", listOf("대여소", "반납")),
    END_GUIDE("안내 종료", listOf("안내 종료", "종료"));

    companion object {
        fun fromText(text: String): List<VoiceCommand> {
            return entries.filter { command ->
                command.keywords.any { keyword -> keyword in text }
            }
        }
    }
}