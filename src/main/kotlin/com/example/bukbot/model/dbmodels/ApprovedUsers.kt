package com.example.bukbot.model.dbmodels

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class ApprovedUsers(
      @Id
      val id: String,
      val chatId: String,
      val firstName: String,
      val lastName: String
)