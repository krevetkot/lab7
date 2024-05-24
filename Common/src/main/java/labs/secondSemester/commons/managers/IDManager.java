package labs.secondSemester.commons.managers;

import labs.secondSemester.commons.objects.Dragon;

import java.util.ArrayList;

/**
 * Класс, работающий с идентификаторами.
 *
 * @author Kseniya
 */
public abstract class IDManager {

    /**
     * Проверяет паспортные данные человека на уникальность.
     *
     * @param passportID - паспортные данные человека
     * @return - уникальность
     */
    public static boolean passportIDisUnique(String passportID) {
        ArrayList<Dragon> collection = CollectionManager.getCollection();
        boolean flag = true;
        for (Dragon element : collection) {
            if (element.getKiller().getPassportID().equals(passportID)) {
                flag = false;
                break;
            }
        }
        return flag;
    }
}
