package com.example.data.local

import android.content.Context
import com.example.data.model.BookEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.FlashcardEntity
import com.example.data.model.QuizQuestionEntity
import com.example.data.model.StudyPlanEntity
import com.example.data.pdf.PdfHelper

object SampleBookData {

    suspend fun populateSampleBooks(context: Context, database: AppDatabase) {
        val bookDao = database.bookDao()
        val flashcardDao = database.flashcardDao()
        val quizDao = database.quizDao()
        val chatDao = database.chatDao()
        val studyPlanDao = database.studyPlanDao()

        // 1. Atomic Habits
        val atomicPages = listOf(
            "Chương 1: Sức Mạnh Phi Thường Của Thói Quen 1%" to """
                Thường thì chúng ta tự thuyết phục mình rằng thành công lớn đòi hỏi hành động lớn. Nhưng tiến bộ thực sự bắt đầu từ những cải thiện 1% vô cùng nhỏ bé. 
                
                Hiệu ứng lãi kép của thói quen:
                - Nếu bạn trở nên tốt hơn 1% mỗi ngày trong một năm, bạn sẽ tiến bộ gấp 37 lần.
                - Nếu bạn tệ đi 1% mỗi ngày trong một năm, bạn sẽ suy giảm gần như về con số 0.
                
                Thói quen là lãi kép của sự tự hoàn thiện. Giống như tiền bạc sinh lãi kép, các tác động từ thói quen của bạn cũng nhân lên khi bạn lặp lại chúng.
                
                Hệ thống quan trọng hơn Mục tiêu:
                - Mục tiêu là kết quả bạn muốn đạt được.
                - Hệ thống là quá trình dẫn đến những kết quả đó.
                Nếu bạn muốn có kết quả tốt hơn, đừng chỉ tập trung vào việc đặt mục tiêu. Hãy tập trung xây dựng hệ thống hành động mỗi ngày.
            """.trimIndent(),

            "Chương 2: Thói Quen Định Hình Danh Tính Của Bạn" to """
                Có 3 tầng thay đổi hành vi:
                1. Thay đổi Kết quả (Outcome): Giảm cân, xuất bản sách, đạt điểm A.
                2. Thay đổi Quy trình (Process): Đổi lịch tập gym, dọn dẹp bàn làm việc.
                3. Thay đổi Danh tính (Identity): Thay đổi niềm tin, nhân sinh quan, cách bạn nhìn nhận bản thân.
                
                Thay đổi bền vững nhất là thay đổi dựa trên Danh tính. Đừng chỉ nói 'Tôi muốn chạy bộ', hãy nói 'Tôi là một người chạy bộ'.
                
                Mỗi hành động bạn thực hiện là một phiếu bầu cho kiểu người mà bạn muốn trở thành. Không có phiếu bầu đơn lẻ nào làm bạn thay đổi ngay lập tức, nhưng khi các phiếu bầu tích lũy, bằng chứng về danh tính mới sẽ chiến thắng.
            """.trimIndent(),

            "Chương 3: 4 Quy Luật Thay Đổi Hành Vi" to """
                Vòng lặp thói quen gồm 4 bước: Gợi ý (Cue) -> Khao khát (Craving) -> Phản hồi (Response) -> Phần thưởng (Reward).
                
                Để Xây Dựng Thói Quen Tốt:
                1. Quy luật 1 (Gợi ý): Làm cho nó rõ ràng (Make it Obvious).
                2. Quy luật 2 (Khao khát): Làm cho nó hấp dẫn (Make it Attractive).
                3. Quy luật 3 (Phản hồi): Làm cho nó dễ dàng (Make it Easy) - Quy tắc 2 phút.
                4. Quy luật 4 (Phần thưởng): Làm cho nó thỏa mãn (Make it Satisfying).
                
                Để Phá Bỏ Thói Quen Xấu (Đảo ngược 4 quy luật):
                1. Làm cho nó vô hình.
                2. Làm cho nó kém hấp dẫn.
                3. Làm cho nó khó khăn.
                4. Làm cho nó không thỏa mãn.
            """.trimIndent()
        )

        val (atomicPath, atomicPageCount) = PdfHelper.createSampleBookPdf(
            context = context,
            fileName = "atomic_habits_sample.pdf",
            bookTitle = "Atomic Habits - Thay Đổi Tí Hon, Hiệu Quả Bất Ngờ",
            author = "James Clear",
            pagesContent = atomicPages
        )

        val atomicBook = BookEntity(
            title = "Atomic Habits - Thay Đổi Tí Hon, Hiệu Quả Bất Ngờ",
            author = "James Clear",
            totalPages = atomicPageCount,
            filePath = atomicPath,
            isSample = true,
            coverColorHex = "#4F46E5",
            summary = "Cuốn sách hướng dẫn phương pháp khoa học đã được chứng minh để xây dựng thói quen tốt và từ bỏ thói quen xấu thông qua quy tắc 1% lãi kép và 4 quy luật thay đổi hành vi.",
            coreThemes = "Lãi kép thói quen, Thay đổi dựa trên danh tính, Vòng lặp thói quen 4 bước, Quy tắc 2 phút, Thiết kế môi trường sống.",
            keyTakeaways = "• Cải thiện 1% mỗi ngày mang lại kết quả gấp 37 lần sau 1 năm.\n• Tập trung vào Hệ thống thay vì chỉ chăm chăm vào Mục tiêu.\n• Muốn tạo thói quen: Rõ ràng, Hấp dẫn, Dễ dàng, Thỏa mãn.\n• Môi trường quan trọng hơn động lực và ý chí.",
            studyProgress = 65
        )
        val atomicId = bookDao.insertBook(atomicBook)

        // Atomic Pages (Indexed)
        val atomicPageEntities = mutableListOf(
            com.example.data.model.BookPageEntity(
                bookId = atomicId,
                pageNumber = 1,
                textContent = "Bìa sách: Atomic Habits - Thay Đổi Tí Hon, Hiệu Quả Bất Ngờ. Tác giả: James Clear. AI Study Edition.",
                wordCount = 18
            )
        )
        atomicPages.forEachIndexed { idx, pair ->
            atomicPageEntities.add(
                com.example.data.model.BookPageEntity(
                    bookId = atomicId,
                    pageNumber = idx + 2,
                    textContent = "${pair.first}\n\n${pair.second}",
                    wordCount = pair.second.split(Regex("\\s+")).filter { it.isNotBlank() }.size
                )
            )
        }
        bookDao.insertPages(atomicPageEntities)
        val atomicChapters = listOf(
            ChapterEntity(bookId = atomicId, chapterNumber = 1, title = "Sức Mạnh Phi Thường Của Thói Quen 1%", startPage = 2, endPage = 2, summary = "Hiểu về sức mạnh của lãi kép và lý do tại sao hệ thống quan trọng hơn mục tiêu.", keyTakeaways = "Tập trung xây dựng hệ thống bền vững"),
            ChapterEntity(bookId = atomicId, chapterNumber = 2, title = "Thói Quen Định Hình Danh Tính Của Bạn", startPage = 3, endPage = 3, summary = "Cách định hình danh tính mới để thay đổi hành vi vĩnh viễn.", keyTakeaways = "Mỗi hành động là một lá phiếu cho con người bạn muốn trở thành"),
            ChapterEntity(bookId = atomicId, chapterNumber = 3, title = "4 Quy Luật Thay Đổi Hành Vi", startPage = 4, endPage = 4, summary = "Khung 4 bước: Rõ ràng, Hấp dẫn, Dễ dàng và Thỏa mãn.", keyTakeaways = "Nắm vững vòng lặp Gợi ý -> Khao khát -> Phản hồi -> Phần thưởng")
        )
        bookDao.insertChapters(atomicChapters)

        // Atomic Flashcards
        val atomicCards = listOf(
            FlashcardEntity(bookId = atomicId, chapterTitle = "Chương 1", front = "Quy tắc 1% mỗi ngày mang lại kết quả gì sau 1 năm?", back = "Tiến bộ gấp 37.78 lần (1.01^365 ≈ 37.78).", keyConcept = "Lãi kép thói quen", difficulty = "Dễ", isMastered = true),
            FlashcardEntity(bookId = atomicId, chapterTitle = "Chương 1", front = "Sự khác biệt cốt lõi giữa Mục tiêu (Goals) và Hệ thống (Systems)?", back = "Mục tiêu là kết quả bạn muốn đạt được; Hệ thống là quy trình dẫn bạn đến những kết quả đó.", keyConcept = "Hệ thống vs Mục tiêu", difficulty = "Vừa", isMastered = true),
            FlashcardEntity(bookId = atomicId, chapterTitle = "Chương 2", front = "3 tầng thay đổi hành vi theo James Clear là gì?", back = "1. Kết quả (Outcomes)\n2. Quy trình (Processes)\n3. Danh tính (Identity - cốt lõi nhất).", keyConcept = "Identity-based habits", difficulty = "Vừa", isMastered = false),
            FlashcardEntity(bookId = atomicId, chapterTitle = "Chương 3", front = "4 bước trong Vòng lặp Thói quen (Habit Loop) là gì?", back = "1. Gợi ý (Cue)\n2. Khao khát (Craving)\n3. Phản hồi (Response)\n4. Phần thưởng (Reward).", keyConcept = "Vòng lặp thói quen", difficulty = "Vừa", isMastered = true),
            FlashcardEntity(bookId = atomicId, chapterTitle = "Chương 3", front = "Quy tắc 2 phút trong xây dựng thói quen mới là gì?", back = "Khi bạn bắt đầu một thói quen mới, hành động đó chỉ nên mất chưa đầy 2 phút để hoàn thành (ví dụ: 'đọc 1 trang sách' thay vì 'đọc 1 tiếng').", keyConcept = "Quy tắc 2 phút", difficulty = "Dễ", isMastered = false)
        )
        flashcardDao.insertFlashcards(atomicCards)

        // Atomic Quizzes
        val atomicQuizzes = listOf(
            QuizQuestionEntity(
                bookId = atomicId,
                question = "Theo James Clear, nếu bạn tiến bộ 1% mỗi ngày liên tục trong 365 ngày, bạn sẽ tiến bộ khoảng bao nhiêu lần?",
                optionA = "Khoảng 3.65 lần",
                optionB = "Khoảng 10 lần",
                optionC = "Khoảng gần 38 lần (37.78)",
                optionD = "Khoảng 100 lần",
                correctOptionIndex = 2,
                explanation = "Theo công thức lãi kép (1.01)^365 ≈ 37.78, sự tích lũy của 1% mỗi ngày tạo nên bước nhảy vọt phi thường.",
                referenceChapter = "Chương 1: Sức mạnh 1%"
            ),
            QuizQuestionEntity(
                bookId = atomicId,
                question = "Khi muốn xây dựng một thói quen mới bền vững, tầng thay đổi nào mang tính quyết định nhất?",
                optionA = "Thay đổi Kết quả (Outcomes)",
                optionB = "Thay đổi Danh tính (Identity)",
                optionC = "Thay đổi Công cụ làm việc",
                optionD = "Thay đổi Ý chí ngắn hạn",
                correctOptionIndex = 1,
                explanation = "Thay đổi dựa trên danh tính (Tôi là ai) giúp hành vi trở nên tự nhiên và bền vững nhất.",
                referenceChapter = "Chương 2: Danh tính"
            ),
            QuizQuestionEntity(
                bookId = atomicId,
                question = "Quy luật thứ 3 để tạo lập thói quen tốt là gì?",
                optionA = "Làm cho nó rõ ràng (Make it Obvious)",
                optionB = "Làm cho nó hấp dẫn (Make it Attractive)",
                optionC = "Làm cho nó dễ dàng (Make it Easy)",
                optionD = "Làm cho nó thỏa mãn (Make it Satisfying)",
                correctOptionIndex = 2,
                explanation = "Quy luật 1: Rõ ràng, Quy luật 2: Hấp dẫn, Quy luật 3: Dễ dàng, Quy luật 4: Thỏa mãn.",
                referenceChapter = "Chương 3: 4 Quy luật"
            )
        )
        quizDao.insertQuizQuestions(atomicQuizzes)

        // Atomic Study Plan
        val atomicPlans = listOf(
            StudyPlanEntity(bookId = atomicId, title = "Lộ trình 7 Ngày Làm Chủ Thói Quen", dayNumber = 1, dayTitle = "Ngày 1: Kiểm toán thói quen hiện tại", focusGoal = "Lập bảng điểm thói quen (Habit Scorecard)", actionItems = "Liệt kê mọi hành động từ lúc thức dậy đến đi ngủ, đánh dấu (+), (-), (=).", isCompleted = true),
            StudyPlanEntity(bookId = atomicId, title = "Lộ trình 7 Ngày Làm Chủ Thói Quen", dayNumber = 2, dayTitle = "Ngày 2: Định hình Danh tính mong muốn", focusGoal = "Xác định 2-3 câu khẳng định danh tính", actionItems = "Viết ra: 'Tôi là người không bao giờ bỏ lỡ buổi tập' và hành động 1 việc nhỏ tương ứng.", isCompleted = true),
            StudyPlanEntity(bookId = atomicId, title = "Lộ trình 7 Ngày Làm Chủ Thói Quen", dayNumber = 3, dayTitle = "Ngày 3: Thiết kế Gợi ý Môi trường", focusGoal = "Làm cho thói quen tốt trở nên rõ ràng", actionItems = "Đặt sách trên gối, để bình nước ngay trên bàn làm việc, giấu điện thoại sang phòng khác.", isCompleted = false),
            StudyPlanEntity(bookId = atomicId, title = "Lộ trình 7 Ngày Làm Chủ Thói Quen", dayNumber = 4, dayTitle = "Ngày 4: Áp dụng Quy tắc 2 phút", focusGoal = "Thu nhỏ hành động tối đa", actionItems = "Thực hiện thói quen mới trong đúng 2 phút rồi dừng lại, tạo đà quán tính.", isCompleted = false)
        )
        studyPlanDao.insertStudyPlans(atomicPlans)

        // Atomic Chat Messages
        val atomicChats = listOf(
            ChatMessageEntity(bookId = atomicId, sender = "gemini", content = "Xin chào! Tôi là Trợ lý AI chuyên sâu về cuốn sách 'Atomic Habits'. Bạn muốn tôi tóm tắt ý tưởng nào, giải thích quy luật nào, hoặc tạo bài tập ứng dụng cho bản thân hôm nay?", timestamp = System.currentTimeMillis() - 60000),
            ChatMessageEntity(bookId = atomicId, sender = "user", content = "Làm sao để tôi áp dụng quy tắc 2 phút vào việc học tiếng Anh?", timestamp = System.currentTimeMillis() - 30000),
            ChatMessageEntity(bookId = atomicId, sender = "gemini", content = "Tuyệt vời! Theo James Clear trong Atomic Habits:\n\n1. **Chuẩn hóa trước khi Tối ưu hóa**: Đừng đặt mục tiêu 'Học 1 tiếng tiếng Anh' (quá nặng, não bộ sẽ trì hoãn). Hãy thu nhỏ lại thành: **'Mở ứng dụng từ điển và học đúng 1 từ'** hoặc **'Nghe đúng 1 đoạn podcast 2 phút'**.\n\n2. **Tạo nghi thức bắt đầu**: Khi bạn hoàn thành 2 phút đầu tiên, bạn đã vượt qua rào cản ma sát lớn nhất. Thường sau 2 phút, quán tính sẽ giữ bạn tiếp tục học thêm 10-15 phút một cách tự nhiên!\n\nBạn muốn thử thiết lập mốc gợi ý (Cue) cụ thể như thế nào?", timestamp = System.currentTimeMillis() - 10000)
        )
        for (chat in atomicChats) {
            chatDao.insertMessage(chat)
        }

        // 2. Tư Duy Nhanh và Chậm (Thinking, Fast and Slow)
        val thinkingPages = listOf(
            "Chương 1: Hai Hệ Thống Tư Duy (System 1 & System 2)" to """
                Não bộ con người hoạt động dựa trên sự tương tác của hai hệ thống tư duy:
                
                • Hệ thống 1 (Tư duy Nhanh): Hoạt động tự động, tức thì, tốn ít hoặc không tốn năng lượng, không có cảm giác bị kiểm soát có chủ ý. 
                  Ví dụ: Nhận diện khuôn mặt quen, phanh xe khi thấy đèn đỏ, tính 2 + 2.
                
                • Hệ thống 2 (Tư duy Chậm): Phân bổ sự chú ý đến các hoạt động đòi hỏi nỗ lực trí óc, bao gồm các tính toán phức tạp, suy luận logic và tự kiểm soát.
                  Ví dụ: Tính 17 x 24, đỗ xe vào chỗ hẹp, so sánh hai gói bảo hiểm.
                
                Hầu hết quyết định của chúng ta do Hệ thống 1 đề xuất, và Hệ thống 2 thường lười biếng chấp thuận mà không kiểm tra kỹ lưỡng.
            """.trimIndent(),

            "Chương 2: Hiệu Ứng Mỏ Neo & Thiên Kiến Nhận Thức" to """
                Các thiên kiến nhận thức phổ biến chi phối cuộc sống:
                
                1. Hiệu ứng Mỏ neo (Anchoring Effect): Khi xem xét một giá trị bất kỳ trước khi ước tính một con số, con số ước tính sẽ bị kéo về gần với giá trị ban đầu đó.
                2. Thiên kiến Xác nhận (Confirmation Bias): Xu hướng chỉ tìm kiếm, diễn giải và ghi nhớ thông tin củng cố cho niềm tin sẵn có của mình.
                3. Hiệu ứng Khung (Framing Effect): Cách diễn đạt cùng một thông tin (VD: 'Tỷ lệ sống 90%' so với 'Tỷ lệ tử vong 10%') tạo ra những phản ứng hoàn toàn khác nhau.
                4. Nỗi sợ Mất mát (Loss Aversion): Nỗi đau khi mất 100 đô la luôn lớn gấp đôi niềm vui khi nhận được 100 đô la.
            """.trimIndent()
        )

        val (thinkingPath, thinkingPageCount) = PdfHelper.createSampleBookPdf(
            context = context,
            fileName = "thinking_fast_slow_sample.pdf",
            bookTitle = "Tư Duy Nhanh và Chậm / Thinking, Fast and Slow",
            author = "Daniel Kahneman (Nobel Kinh tế)",
            pagesContent = thinkingPages
        )

        val thinkingBook = BookEntity(
            title = "Tư Duy Nhanh và Chậm (Thinking, Fast and Slow)",
            author = "Daniel Kahneman (Nobel Kinh tế)",
            totalPages = thinkingPageCount,
            filePath = thinkingPath,
            isSample = true,
            coverColorHex = "#D97706",
            summary = "Khám phá bản chất của tâm trí con người qua hai hệ thống tư duy: Hệ thống 1 (nhanh, cảm tính, trực giác) và Hệ thống 2 (chậm, logic, đòi hỏi nỗ lực) cùng các bẫy tâm lý phổ biến.",
            coreThemes = "System 1 & System 2, Thiên kiến nhận thức, Hiệu ứng mỏ neo, Nỗi sợ mất mát, Hiệu ứng sẵn có.",
            keyTakeaways = "• Nhận thức được khi nào Hệ thống 1 đang đưa ra phán đoán sai lầm.\n• Cảnh giác với thiên kiến xác nhận và hiệu ứng mỏ neo khi đàm phán.\n• Con người thường ghét mất mát hơn là thích được nhận (Loss Aversion).\n• Kích hoạt Hệ thống 2 khi đứng trước các quyết định tài chính và chiến lược quan trọng.",
            studyProgress = 40
        )
        val thinkingId = bookDao.insertBook(thinkingBook)

        // Thinking Pages (Indexed)
        val thinkingPageEntities = mutableListOf(
            com.example.data.model.BookPageEntity(
                bookId = thinkingId,
                pageNumber = 1,
                textContent = "Bìa sách: Tư Duy Nhanh và Chậm (Thinking, Fast and Slow). Tác giả: Daniel Kahneman (Nobel Kinh tế). AI Study Edition.",
                wordCount = 20
            )
        )
        thinkingPages.forEachIndexed { idx, pair ->
            thinkingPageEntities.add(
                com.example.data.model.BookPageEntity(
                    bookId = thinkingId,
                    pageNumber = idx + 2,
                    textContent = "${pair.first}\n\n${pair.second}",
                    wordCount = pair.second.split(Regex("\\s+")).filter { it.isNotBlank() }.size
                )
            )
        }
        bookDao.insertPages(thinkingPageEntities)

        val thinkingChapters = listOf(
            ChapterEntity(bookId = thinkingId, chapterNumber = 1, title = "Hai Hệ Thống Tư Duy", startPage = 2, endPage = 2, summary = "Đặc điểm và sự tương tác giữa Hệ thống 1 và Hệ thống 2.", keyTakeaways = "Hệ thống 2 lười biếng nhưng cần thiết cho quyết định khó"),
            ChapterEntity(bookId = thinkingId, chapterNumber = 2, title = "Hiệu Ứng Mỏ Neo & Thiên Kiến", startPage = 3, endPage = 3, summary = "Những cạm bẫy tâm lý dẫn tới các quyết định sai lầm.", keyTakeaways = "Luôn tự hỏi liệu mình có đang bị neo tâm lý hay không")
        )
        bookDao.insertChapters(thinkingChapters)

        val thinkingCards = listOf(
            FlashcardEntity(bookId = thinkingId, chapterTitle = "Chương 1", front = "Đặc điểm chính của Hệ thống 1 (System 1) là gì?", back = "Tự động, nhanh chóng, không tốn nỗ lực, điều khiển bởi trực giác và cảm xúc.", keyConcept = "System 1", difficulty = "Dễ", isMastered = true),
            FlashcardEntity(bookId = thinkingId, chapterTitle = "Chương 1", front = "Đặc điểm chính của Hệ thống 2 (System 2) là gì?", back = "Chậm rãi, đòi hỏi tập trung cao, tiêu tốn năng lượng trí óc, phụ trách tư duy logic và tính toán phức tạp.", keyConcept = "System 2", difficulty = "Dễ", isMastered = true),
            FlashcardEntity(bookId = thinkingId, chapterTitle = "Chương 2", front = "Hiệu ứng Mỏ neo (Anchoring Effect) hoạt động như thế nào?", back = "Con người có xu hướng bị ảnh hưởng quá mức bởi thông tin đầu tiên nhận được (mỏ neo) khi đưa ra các ước lượng hoặc quyết định tiếp theo.", keyConcept = "Anchoring Effect", difficulty = "Vừa", isMastered = false),
            FlashcardEntity(bookId = thinkingId, chapterTitle = "Chương 2", front = "Loss Aversion (Tâm lý ghét mất mát) có tỷ lệ cảm xúc như thế nào?", back = "Nỗi đau mất đi một khoản tiền/giá trị thường có cường độ mạnh gấp 1.5 đến 2.5 lần niềm vui khi kiếm được cùng khoản đó.", keyConcept = "Loss Aversion", difficulty = "Vừa", isMastered = false)
        )
        flashcardDao.insertFlashcards(thinkingCards)

        val thinkingQuizzes = listOf(
            QuizQuestionEntity(
                bookId = thinkingId,
                question = "Phép tính nào sau đây chủ yếu kích hoạt Hệ thống 2 (System 2) của bạn?",
                optionA = "2 + 2",
                optionB = "5 x 10",
                optionC = "17 x 24",
                optionD = "100 - 50",
                correctOptionIndex = 2,
                explanation = "Tính 17 x 24 đòi hỏi nỗ lực trí óc có ý thức, ghi nhớ tạm thời và phân tích nhiều bước của Hệ thống 2.",
                referenceChapter = "Chương 1"
            ),
            QuizQuestionEntity(
                bookId = thinkingId,
                question = "Khái niệm 'Loss Aversion' (Ghét mất mát) chỉ ra điều gì?",
                optionA = "Mọi người thích rủi ro hơn an toàn",
                optionB = "Cảm giác đau đớn khi mất mát lớn gấp đôi cảm giác vui sướng khi nhận được điều tương tự",
                optionC = "Não bộ không quan tâm đến kết quả xấu",
                optionD = "Mất mát giúp kích hoạt trực giác nhanh hơn",
                correctOptionIndex = 1,
                explanation = "Con người có xu hướng tránh mất mát mạnh mẽ hơn nhiều so với động lực tìm kiếm lợi ích tương đương.",
                referenceChapter = "Chương 2"
            )
        )
        quizDao.insertQuizQuestions(thinkingQuizzes)

        val thinkingPlans = listOf(
            StudyPlanEntity(bookId = thinkingId, title = "Lộ Trình 5 Ngày Phân Tích Tư Duy Phản Biện", dayNumber = 1, dayTitle = "Ngày 1: Nhận diện System 1 & 2 trong ngày", focusGoal = "Ghi lại 3 quyết định cảm tính và 1 quyết định lý trí", actionItems = "Quan sát cảm xúc khi phản xạ tức thì.", isCompleted = true),
            StudyPlanEntity(bookId = thinkingId, title = "Lộ Trình 5 Ngày Phân Tích Tư Duy Phản Biện", dayNumber = 2, dayTitle = "Ngày 2: Phá vỡ bẫy Mỏ neo trong mua sắm", focusGoal = "Tìm hiểu giá gốc trước khi xem giá khuyến mãi", actionItems = "Thực hành đàm phán không dựa trên con số đối phương đưa ra đầu tiên.", isCompleted = false)
        )
        studyPlanDao.insertStudyPlans(thinkingPlans)

        val thinkingChats = listOf(
            ChatMessageEntity(bookId = thinkingId, sender = "gemini", content = "Chào bạn! Tôi là trợ lý AI phân tích cuốn sách 'Thinking, Fast and Slow' của Daniel Kahneman. Bạn muốn phân tích tình huống thực tế nào theo lăng kính System 1 & System 2?", timestamp = System.currentTimeMillis() - 50000)
        )
        for (chat in thinkingChats) {
            chatDao.insertMessage(chat)
        }
    }
}
