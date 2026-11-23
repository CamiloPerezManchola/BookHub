package usco.edu.co.BookHub.dto.response;

import lombok.Data;

@Data
public class ReadingProgressResponse {

    private Long bookId;

    private Long chapterId;

    private Integer chapterNumber;

    private Double scrollPercentage;
}
