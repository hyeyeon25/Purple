package Purple.Purple.itinerery.exception;

// 경로 최적화 실패 시 발생하는 예외
public class RouteOptimizationException extends RuntimeException {
    public RouteOptimizationException(String message) {
        super(message);
    }

    public RouteOptimizationException(String message, Throwable cause) {
        super(message, cause);
    }
}

