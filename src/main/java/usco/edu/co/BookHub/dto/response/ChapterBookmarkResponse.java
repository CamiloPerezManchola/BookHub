package usco.edu.co.BookHub.dto.response;

import lombok.Data;

@Data
public class ChapterBookmarkResponse {

    private Long chapterId;

    private Integer chapterNumber;

    private String chapterTitle;
}
