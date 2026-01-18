package Purple.Purple.folder.exception;

// 폴더를 찾을 수 없을 때 발생하는 예외
public class FolderNotFoundException extends RuntimeException {
    public FolderNotFoundException(Integer folderId) {
        super("폴더를 찾을 수 없습니다. id=" + folderId);
    }

    public FolderNotFoundException(Integer folderId, Long userId) {
        super("폴더를 찾을 수 없거나 소유자가 아닙니다. folderId=" + folderId + ", userId=" + userId);
    }
}


