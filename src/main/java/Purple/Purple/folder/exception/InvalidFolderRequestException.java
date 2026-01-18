package Purple.Purple.folder.exception;

// 잘못된 폴더 요청 시 발생하는 예외
public class InvalidFolderRequestException extends RuntimeException {
    public InvalidFolderRequestException(String message) {
        super(message);
    }
}

