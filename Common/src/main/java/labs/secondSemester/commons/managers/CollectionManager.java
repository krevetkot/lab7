package labs.secondSemester.commons.managers;

import jakarta.xml.bind.JAXBException;
import labs.secondSemester.commons.exceptions.AccessDeniedException;
import labs.secondSemester.commons.exceptions.FailedBuildingException;
import labs.secondSemester.commons.objects.Dragon;
import lombok.Setter;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Класс, управляющий коллекцией элементов типа {@link Dragon}.
 *
 * @author Kseniya
 */
public class CollectionManager {
    /**
     * Коллекция
     */
    private static ArrayList<Dragon> collectionOfDragons;
    /**
     * Название файла типа xml, используемого при загрузке и сохранении коллекции
     */
    @Setter
    private static String fileName;
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private static final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    /**
     * Загружает коллекцию из файла. Вызывается при открытии приложения.
     *
     * @param filename - название файла типа xml, из которого происходит загрузка
     * @throws IOException             - ошибка открытия файла/доступа к файлу/отсутствие файла/невалидный путь
     * @throws JAXBException           - ошибка парсинга
     * @throws FailedBuildingException - ошибка сборки объектов
     */
    /*public static void loadCollection(String filename) throws IOException, JAXBException, FailedBuildingException {
        if (!(new File(filename).isFile())){
            throw new IOException("Невозможно прочесть файл.");
        }

        setFileName(filename);

        BufferedReader br = new BufferedReader(new FileReader(CollectionManager.fileName));
        String body = br.lines().collect(Collectors.joining());
        StringReader reader = new StringReader(body);
        JAXBContext context = JAXBContext.newInstance(DragonsForParsing.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        DragonsForParsing dragons = (DragonsForParsing) unmarshaller.unmarshal(reader);

        boolean flag = true;
        for (Dragon dragon : dragons.getCollectionOfDragons()) {
            if (!Validator.dragonValidation(dragon) || !IDManager.dragonIDisUnique(dragon.getId(), dragons.getCollectionOfDragons())) {
                flag = false;
                break;
            }
        }
        if (flag) {
            collectionOfDragons = dragons.getCollectionOfDragons();
            Collections.sort(collectionOfDragons);
            System.out.println("Коллекция загружена.");
        } else {
            throw new FailedBuildingException("Данные в коллекции не валидны", Dragon.class);
        }

        br.close();
    }*/

    /**
     * Возвращает экземпляр коллекции. Если коллекция еще не инициализирована - инициализирует.
     *
     * @return экземпляр коллекции
     */
    public static ArrayList<Dragon> getCollection() {
        if (collectionOfDragons == null) {
            collectionOfDragons = new ArrayList<>();
        }
        return collectionOfDragons;
    }

    public static ArrayList<Dragon> getCollectionForReading() throws AccessDeniedException {
        try {
            if (writeLock.tryLock(1L, TimeUnit.MINUTES)) {
                if (collectionOfDragons == null) {
                    collectionOfDragons = new ArrayList<>();
                }
                return collectionOfDragons;
            } else {
                throw new AccessDeniedException("Невозможно обратиться к коллекции для чтения.");
            }
        } catch (Exception e) {
            throw new AccessDeniedException("Невозможно обратиться к коллекции для чтения.");
        } finally {
            writeLock.unlock();
        }
    }

    public static ArrayList<Dragon> getCollectionForWriting() throws AccessDeniedException {
        try {
            if (readLock.tryLock(1L, TimeUnit.MINUTES)) {
                if (collectionOfDragons == null) {
                    collectionOfDragons = new ArrayList<>();
                }
                return collectionOfDragons;
            } else {
                throw new AccessDeniedException("Невозможно обратиться к коллекции для записи.");
            }
        } catch (Exception e) {
            throw new AccessDeniedException("Невозможно обратиться к коллекции для записи.");
        } finally {
            readLock.unlock();
        }
    }



    /**
     * Возвращает элемент коллекции по его идентификатору.
     *
     * @param id - идентификатор
     */
    public static Dragon getById(long id) {
        return collectionOfDragons.stream().filter(x -> x.getId() == id).findAny().orElse(null);
    }

    public static boolean contains(Dragon dragon){
        for (Dragon element: collectionOfDragons){
            if (dragon.equals(element)){
                return true;
            }
        }
        return false;
    }

}
