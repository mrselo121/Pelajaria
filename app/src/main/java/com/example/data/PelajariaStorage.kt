package com.example.data

import android.content.Context
import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject

class PelajariaStorage(context: Context) {

    private val prefs = context.getSharedPreferences("pelajaria_local_store_v2", Context.MODE_PRIVATE)
    private val keyState = "app_state_json"
    private val keySession = "active_session_json"

    fun loadState(): PelajariaAppState {
        val raw = prefs.getString(keyState, null)
        if (raw.isNullOrBlank()) {
            val seeded = seedDemoState()
            saveState(seeded)
            return seeded
        }
        return runCatching { decodeState(JSONObject(raw)) }.getOrElse {
            val seeded = seedDemoState()
            saveState(seeded)
            seeded
        }
    }

    fun saveState(state: PelajariaAppState) {
        prefs.edit().putString(keyState, encodeState(state).toString()).apply()
    }

    fun loadSession(): UserSession? {
        val raw = prefs.getString(keySession, null) ?: return null
        return runCatching {
            val obj = JSONObject(raw)
            UserSession(
                role = UserRole.valueOf(obj.getString("role")),
                userId = obj.getString("userId")
            )
        }.getOrNull()
    }

    fun saveSession(session: UserSession?) {
        if (session == null) {
            prefs.edit().remove(keySession).apply()
        } else {
            val obj = JSONObject().apply {
                put("role", session.role.name)
                put("userId", session.userId)
            }
            prefs.edit().putString(keySession, obj.toString()).apply()
        }
    }

    fun resetToDemo(): PelajariaAppState {
        val seeded = seedDemoState()
        saveState(seeded)
        return seeded
    }

    fun seedDemoState(): PelajariaAppState {
        val ibuSiti = TeacherAccount(
            id = "teacher_siti",
            name = "Ibu Siti",
            email = "siti@pelajaria.test",
            password = "pelajaria",
            subject = "Matematika",
            classLabel = "Kelas 2 SD"
        )

        val budiSantoso = ParentAccount(
            id = "parent_budi",
            name = "Budi Santoso",
            email = "parent@pelajaria.test",
            password = "pelajaria"
        )

        val alya = StudentProfile(
            id = "student_alya",
            parentId = "parent_budi",
            name = "Alya",
            level = EducationLevel.SD,
            grade = "2 SD",
            avatarEmoji = "🦊",
            avatarColorHex = 0xFFFB8C00
        )

        val bima = StudentProfile(
            id = "student_bima",
            parentId = "parent_other",
            name = "Bima Pratama",
            level = EducationLevel.SD,
            grade = "2 SD",
            avatarEmoji = "🦁",
            avatarColorHex = 0xFF1E88E5
        )

        val allStudentIds = listOf(alya.id, bima.id)

        val docPenjumlahan = DocumentItem(
            id = "doc_penjumlahan",
            title = "Lembar Kerja Penjumlahan",
            description = "Perhatikan foto lembar soal penjumlahan berikut. Kerjakan dengan mengetik jawaban atau unggah foto coretan berhitungmu.",
            subject = "Matematika",
            level = EducationLevel.SD,
            grade = "2 SD",
            format = DocumentFormat.IMAGE,
            presentationMode = PresentationMode.ORIGINAL_WORKSHEET,
            teacherId = ibuSiti.id,
            teacherName = ibuSiti.name,
            targetStudentIds = allStudentIds,
            dueDate = "Besok, 19:00",
            samplePresetKey = "math_worksheet",
            submissions = mapOf(
                bima.id to StudentSubmission(
                    documentId = "doc_penjumlahan",
                    studentId = bima.id,
                    status = SubmissionStatus.WAITING_GRADE,
                    textAnswer = "1) 19 apel. 2) 12 + 15 = 27",
                    submittedAtLabel = "Hari ini, 08:15"
                )
            ),
            createdAtLabel = "Hari ini"
        )

        val docHewan = DocumentItem(
            id = "doc_hewan",
            title = "Materi Mengenal Hewan",
            description = "Materi PDF yang sudah diubah Ibu Siti menjadi pembelajaran interaktif bertahap.",
            subject = "IPA",
            level = EducationLevel.SD,
            grade = "2 SD",
            format = DocumentFormat.PDF,
            presentationMode = PresentationMode.INTERACTIVE_MATERIAL,
            teacherId = ibuSiti.id,
            teacherName = ibuSiti.name,
            targetStudentIds = allStudentIds,
            samplePresetKey = "animal_material",
            pdfSimulatedPages = DocumentAiProcessor.SampleDocumentPreset.ANIMAL_MATERIAL.pdfPages,
            interactiveSections = DocumentAiProcessor.extractInteractiveFromDocument(
                docTitle = "Materi Mengenal Hewan",
                docDescription = "Jenis hewan dan habitatnya",
                subject = "IPA",
                level = EducationLevel.SD,
                presetKey = "animal_material",
                sourcePages = DocumentAiProcessor.SampleDocumentPreset.ANIMAL_MATERIAL.pdfPages
            ),
            createdAtLabel = "Kemarin"
        )

        val docBahasa = DocumentItem(
            id = "doc_bahasa",
            title = "Latihan Bahasa Indonesia",
            description = "Soal digital hasil konversi dari foto tulisan tangan Ibu Siti. Siap diperiksa dan dinilai manual.",
            subject = "Bahasa Indonesia",
            level = EducationLevel.SD,
            grade = "2 SD",
            format = DocumentFormat.IMAGE,
            presentationMode = PresentationMode.DIGITAL_QUESTIONS,
            teacherId = ibuSiti.id,
            teacherName = ibuSiti.name,
            targetStudentIds = allStudentIds,
            dueDate = "Hari ini, 20:00",
            samplePresetKey = "indo_handwritten",
            digitalQuestions = listOf(
                DigitalQuestion(
                    id = "dq_indo_1",
                    prompt = "1. Huruf vokal yang terdapat pada kata 'BUKU' adalah...",
                    isEssay = false,
                    options = listOf("B dan K", "U saja", "B dan U", "K dan U"),
                    sourceNote = "Diekstrak dari tulisan tangan no. 1"
                ),
                DigitalQuestion(
                    id = "dq_indo_2",
                    prompt = "2. Tulislah 1 kalimat sapaan santun saat bertemu guru di pagi hari!",
                    isEssay = true,
                    options = emptyList(),
                    sourceNote = "Soal esai tulisan tangan no. 2"
                )
            ),
            hasOcrWarning = true,
            ocrWarningText = "Sebagian tulisan tangan pada foto sumber telah diperiksa dan disetujui oleh Ibu Siti.",
            submissions = mapOf(
                alya.id to StudentSubmission(
                    documentId = "doc_bahasa",
                    studentId = alya.id,
                    status = SubmissionStatus.WAITING_GRADE,
                    textAnswer = "Sudah saya kerjakan dengan teliti, Bu Siti.",
                    digitalAnswers = mapOf(
                        "dq_indo_1" to "U saja",
                        "dq_indo_2" to "Selamat pagi, Ibu Siti. Semoga sehat selalu."
                    ),
                    submittedAtLabel = "Hari ini, 09:10"
                )
            ),
            createdAtLabel = "2 hari lalu"
        )

        val docLingkungan = DocumentItem(
            id = "doc_lingkungan",
            title = "Mengenal Lingkungan Sekitar",
            description = "Dokumen PDF asli mengenai kebersihan rumah dan gotong royong di kelas.",
            subject = "Pendidikan Pancasila",
            level = EducationLevel.SD,
            grade = "2 SD",
            format = DocumentFormat.PDF,
            presentationMode = PresentationMode.ORIGINAL_MATERIAL,
            teacherId = ibuSiti.id,
            teacherName = ibuSiti.name,
            targetStudentIds = allStudentIds,
            samplePresetKey = "environment_pdf",
            pdfSimulatedPages = DocumentAiProcessor.SampleDocumentPreset.ENVIRONMENT_PDF.pdfPages,
            createdAtLabel = "3 hari lalu"
        )

        val docBerhitungSelesai = DocumentItem(
            id = "doc_berhitung_selesai",
            title = "Lembar Kerja Berhitung Cepat",
            description = "Latihan dasar penjumlahan dan pengurangan angka 1 sampai 20.",
            subject = "Matematika",
            level = EducationLevel.SD,
            grade = "2 SD",
            format = DocumentFormat.IMAGE,
            presentationMode = PresentationMode.ORIGINAL_WORKSHEET,
            teacherId = ibuSiti.id,
            teacherName = ibuSiti.name,
            targetStudentIds = allStudentIds,
            dueDate = "Sudah Selesai",
            samplePresetKey = "math_worksheet",
            submissions = mapOf(
                alya.id to StudentSubmission(
                    documentId = "doc_berhitung_selesai",
                    studentId = alya.id,
                    status = SubmissionStatus.GRADED,
                    textAnswer = "1) 19 apel, 2) 12 + 15 = 27",
                    sampleAnswerPhotoKey = "handwritten_math_sample",
                    submittedAtLabel = "2 hari lalu",
                    score = 92,
                    teacherNote = "Bagus sekali, Alya! Langkah penjumlahanmu rapi dan jelas.",
                    gradedAtLabel = "Kemarin"
                )
            ),
            createdAtLabel = "4 hari lalu"
        )

        val logs = listOf(
            ActivityRecord(
                id = "act_1",
                studentId = alya.id,
                title = "Mengirim jawaban Latihan Bahasa Indonesia",
                subject = "Bahasa Indonesia",
                category = "Tugas",
                scoreLabel = "Menunggu Nilai",
                timestampLabel = "Hari ini"
            ),
            ActivityRecord(
                id = "act_2",
                studentId = alya.id,
                title = "Lembar Kerja Berhitung Cepat dinilai Ibu Siti",
                subject = "Matematika",
                category = "Nilai",
                scoreLabel = "Nilai 92",
                timestampLabel = "Kemarin"
            )
        )

        return PelajariaAppState(
            teachers = listOf(ibuSiti),
            students = listOf(alya, bima),
            parents = listOf(budiSantoso),
            documents = listOf(
                docPenjumlahan,
                docHewan,
                docBahasa,
                docLingkungan,
                docBerhitungSelesai
            ),
            activityRecords = logs
        )
    }

    private fun encodeState(state: PelajariaAppState): JSONObject {
        return JSONObject().apply {
            put("teachers", JSONArray().apply {
                state.teachers.forEach { t ->
                    put(JSONObject().apply {
                        put("id", t.id)
                        put("name", t.name)
                        put("email", t.email)
                        put("password", t.password)
                        put("subject", t.subject)
                        put("classLabel", t.classLabel)
                    })
                }
            })
            put("students", JSONArray().apply {
                state.students.forEach { s ->
                    put(JSONObject().apply {
                        put("id", s.id)
                        put("parentId", s.parentId)
                        put("name", s.name)
                        put("level", s.level.name)
                        put("grade", s.grade)
                        put("avatarEmoji", s.avatarEmoji)
                        put("avatarColorHex", s.avatarColorHex)
                    })
                }
            })
            put("parents", JSONArray().apply {
                state.parents.forEach { p ->
                    put(JSONObject().apply {
                        put("id", p.id)
                        put("name", p.name)
                        put("email", p.email)
                        put("password", p.password)
                    })
                }
            })
            put("documents", JSONArray().apply {
                state.documents.forEach { d ->
                    put(JSONObject().apply {
                        put("id", d.id)
                        put("title", d.title)
                        put("description", d.description)
                        put("subject", d.subject)
                        put("level", d.level.name)
                        put("grade", d.grade)
                        put("format", d.format.name)
                        put("presentationMode", d.presentationMode.name)
                        put("teacherId", d.teacherId)
                        put("teacherName", d.teacherName)
                        put("targetStudentIds", JSONArray(d.targetStudentIds))
                        put("dueDate", d.dueDate)
                        put("sourceUriString", d.sourceUriString)
                        put("samplePresetKey", d.samplePresetKey)
                        put("pdfSimulatedPages", JSONArray(d.pdfSimulatedPages))
                        put("interactiveSections", JSONArray().apply {
                            d.interactiveSections.forEach { sec ->
                                put(JSONObject().apply {
                                    put("id", sec.id)
                                    put("title", sec.title)
                                    put("explanation", sec.explanation)
                                    put("simpleExample", sec.simpleExample)
                                    put("touchPrompt", sec.touchPrompt)
                                    put("touchReveal", sec.touchReveal)
                                    put("visualEmoji", sec.visualEmoji)
                                })
                            }
                        })
                        put("digitalQuestions", JSONArray().apply {
                            d.digitalQuestions.forEach { q ->
                                put(JSONObject().apply {
                                    put("id", q.id)
                                    put("prompt", q.prompt)
                                    put("isEssay", q.isEssay)
                                    put("options", JSONArray(q.options))
                                    put("sourceNote", q.sourceNote)
                                })
                            }
                        })
                        put("hasOcrWarning", d.hasOcrWarning)
                        put("ocrWarningText", d.ocrWarningText)
                        put("readByStudentIds", JSONArray(d.readByStudentIds))
                        put("submissions", JSONArray().apply {
                            d.submissions.values.forEach { sub ->
                                put(JSONObject().apply {
                                    put("documentId", sub.documentId)
                                    put("studentId", sub.studentId)
                                    put("status", sub.status.name)
                                    put("textAnswer", sub.textAnswer)
                                    put("photoUriString", sub.photoUriString)
                                    put("sampleAnswerPhotoKey", sub.sampleAnswerPhotoKey)
                                    put("digitalAnswers", JSONObject().apply {
                                        sub.digitalAnswers.forEach { (k, v) -> put(k, v) }
                                    })
                                    put("submittedAtLabel", sub.submittedAtLabel)
                                    put("score", sub.score ?: -1)
                                    put("teacherNote", sub.teacherNote)
                                    put("gradedAtLabel", sub.gradedAtLabel)
                                })
                            }
                        })
                        put("createdAtLabel", d.createdAtLabel)
                    })
                }
            })
            put("activityRecords", JSONArray().apply {
                state.activityRecords.forEach { a ->
                    put(JSONObject().apply {
                        put("id", a.id)
                        put("studentId", a.studentId)
                        put("title", a.title)
                        put("subject", a.subject)
                        put("category", a.category)
                        put("scoreLabel", a.scoreLabel)
                        put("timestampLabel", a.timestampLabel)
                    })
                }
            })
        }
    }

    private fun decodeState(root: JSONObject): PelajariaAppState {
        val teachers = decodeArray(root.getJSONArray("teachers")) { o ->
            TeacherAccount(
                id = o.getString("id"),
                name = o.getString("name"),
                email = o.getString("email"),
                password = o.getString("password"),
                subject = o.getString("subject"),
                classLabel = o.optString("classLabel", "Kelas 2 SD")
            )
        }
        val students = decodeArray(root.getJSONArray("students")) { o ->
            StudentProfile(
                id = o.getString("id"),
                parentId = o.getString("parentId"),
                name = o.getString("name"),
                level = EducationLevel.valueOf(o.getString("level")),
                grade = o.getString("grade"),
                avatarEmoji = o.getString("avatarEmoji"),
                avatarColorHex = o.getLong("avatarColorHex")
            )
        }
        val parents = decodeArray(root.getJSONArray("parents")) { o ->
            ParentAccount(
                id = o.getString("id"),
                name = o.getString("name"),
                email = o.getString("email"),
                password = o.getString("password")
            )
        }
        val documents = decodeArray(root.getJSONArray("documents")) { o ->
            val sections = decodeArray(o.getJSONArray("interactiveSections")) { s ->
                InteractiveSection(
                    id = s.getString("id"),
                    title = s.getString("title"),
                    explanation = s.getString("explanation"),
                    simpleExample = s.getString("simpleExample"),
                    touchPrompt = s.getString("touchPrompt"),
                    touchReveal = s.getString("touchReveal"),
                    visualEmoji = s.getString("visualEmoji")
                )
            }
            val questions = decodeArray(o.getJSONArray("digitalQuestions")) { q ->
                DigitalQuestion(
                    id = q.getString("id"),
                    prompt = q.getString("prompt"),
                    isEssay = q.getBoolean("isEssay"),
                    options = decodeStringList(q.getJSONArray("options")),
                    sourceNote = q.optString("sourceNote", "")
                )
            }
            val subsMap = mutableMapOf<String, StudentSubmission>()
            val subsArr = o.getJSONArray("submissions")
            for (i in 0 until subsArr.length()) {
                val s = subsArr.getJSONObject(i)
                val digObj = s.getJSONObject("digitalAnswers")
                val digMap = mutableMapOf<String, String>()
                digObj.keys().forEach { k -> digMap[k] = digObj.getString(k) }
                val rawScore = s.optInt("score", -1)
                val sub = StudentSubmission(
                    documentId = s.getString("documentId"),
                    studentId = s.getString("studentId"),
                    status = SubmissionStatus.valueOf(s.getString("status")),
                    textAnswer = s.optString("textAnswer", ""),
                    photoUriString = s.optString("photoUriString", ""),
                    sampleAnswerPhotoKey = s.optString("sampleAnswerPhotoKey", ""),
                    digitalAnswers = digMap,
                    submittedAtLabel = s.optString("submittedAtLabel", ""),
                    score = if (rawScore >= 0) rawScore else null,
                    teacherNote = s.optString("teacherNote", ""),
                    gradedAtLabel = s.optString("gradedAtLabel", "")
                )
                subsMap[sub.studentId] = sub
            }
            DocumentItem(
                id = o.getString("id"),
                title = o.getString("title"),
                description = o.getString("description"),
                subject = o.getString("subject"),
                level = EducationLevel.valueOf(o.getString("level")),
                grade = o.getString("grade"),
                format = DocumentFormat.valueOf(o.getString("format")),
                presentationMode = PresentationMode.valueOf(o.getString("presentationMode")),
                teacherId = o.getString("teacherId"),
                teacherName = o.getString("teacherName"),
                targetStudentIds = decodeStringList(o.getJSONArray("targetStudentIds")),
                dueDate = o.optString("dueDate", ""),
                sourceUriString = o.optString("sourceUriString", ""),
                samplePresetKey = o.optString("samplePresetKey", "math_worksheet"),
                pdfSimulatedPages = decodeStringList(o.getJSONArray("pdfSimulatedPages")),
                interactiveSections = sections,
                digitalQuestions = questions,
                hasOcrWarning = o.optBoolean("hasOcrWarning", false),
                ocrWarningText = o.optString("ocrWarningText", ""),
                readByStudentIds = decodeStringList(o.getJSONArray("readByStudentIds")),
                submissions = subsMap,
                createdAtLabel = o.optString("createdAtLabel", "Hari ini")
            )
        }
        val records = decodeArray(root.getJSONArray("activityRecords")) { o ->
            ActivityRecord(
                id = o.getString("id"),
                studentId = o.getString("studentId"),
                title = o.getString("title"),
                subject = o.getString("subject"),
                category = o.getString("category"),
                scoreLabel = o.getString("scoreLabel"),
                timestampLabel = o.getString("timestampLabel")
            )
        }
        return PelajariaAppState(
            teachers = teachers,
            students = students,
            parents = parents,
            documents = documents,
            activityRecords = records
        )
    }

    private fun decodeStringList(arr: JSONArray): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until arr.length()) list.add(arr.getString(i))
        return list
    }

    private inline fun <T> decodeArray(arr: JSONArray, mapper: (JSONObject) -> T): List<T> {
        val list = mutableListOf<T>()
        for (i in 0 until arr.length()) list.add(mapper(arr.getJSONObject(i)))
        return list
    }
}
