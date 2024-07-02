package k1ryl.meldunekbot.openai.dto;

import lombok.Data;

@Data
public class StateData<T> {

    private final T data;

    public T getData() {
        return data;
    }
}
