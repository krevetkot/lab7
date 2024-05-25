package labs.secondSemester.commons.network;

import lombok.Getter;

import java.io.Serializable;

/**
 * @param count  количество всех блоков
 * @param number порядковый номер этого блока
 */
@Getter
public record Header(int count, int number) implements Serializable {
}
