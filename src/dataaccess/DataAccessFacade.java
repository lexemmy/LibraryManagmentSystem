package dataaccess;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import business.*;
import dataaccess.DataAccessFacade.StorageType;


public class DataAccessFacade implements DataAccess {

    enum StorageType {
        BOOKS, MEMBERS, USERS;
    }

    public static final String OUTPUT_DIR = System.getProperty("user.dir")
            + "/src/dataaccess/storage"; //for Unix file system
    //			+ "\\src\\dataaccess\\storage"; //for Windows file system
    public static final String DATE_PATTERN = "MM/dd/yyyy";

    //implement: other save operations
    public void saveNewMember(LibraryMember member) {
        HashMap<String, LibraryMember> mems = readMemberMap();
        String memberId = member.getMemberId();
        mems.put(memberId, member);
        saveToStorage(StorageType.MEMBERS, mems);
    }

    @Override
    public void saveNewBook(Book book) {
        List<Book> newBook = new ArrayList<>();
        newBook.add(book);
        loadBookMap(newBook);

        HashMap<String, Book> list = readBooksMap();
        for (Map.Entry m : list.entrySet()) {
            System.out.println(m.getValue());
        }

    }

    @Override
    public void saveNewBookCopy(BookCopy bookCopy) {

    }

    @Override
    public boolean searchMember(String memberId) {
        HashMap<String, LibraryMember> libraryMemberHashMap = readMemberMap();
        return libraryMemberHashMap.containsKey(memberId);
    }

    @Override
    public Book searchBook(String isbn) {
        HashMap<String, Book> bookHashMap = readBooksMap();
        return bookHashMap.containsKey(isbn) ? bookHashMap.get(isbn) : null;
    }

    @Override
    public int getMaximumCheckoutLength(String isbn) {
        return 0;
    }

    @Override
    public BookCopy nextAvailableBookCopy(String isbn) {
        HashMap<String, Book> bookHashMap = readBooksMap();
        Book book = bookHashMap.get(isbn);

        if (book != null) {
            for (BookCopy bookCopy : book.getCopies()) {
                if (bookCopy.isAvailable()) {
                    return bookCopy;
                }
            }
        }
        return null;
    }

    @Override
    public void saveMemberCheckoutRecord(String memberId, CheckOutRecordEntry entry) {
        HashMap<String, LibraryMember> libraryMemberHashMap = readMemberMap();
        LibraryMember libraryMember = libraryMemberHashMap.get(memberId);
        if (libraryMember != null) {
            libraryMember.addCheckOutRecordEntry(entry);
            this.saveNewMember(libraryMember);
        }
    }

    @Override
    public Auth verifyUser(int id, String password) {
        HashMap<String, User> user = readUserMap();
        for (Map.Entry map : user.entrySet()) {
            User getUser = (User) map.getValue();
            if (id == Integer.parseInt(getUser.getId()) && password.equals(getUser.getPassword())) {
                return getUser.getAuthorization();
            }
        }
        return null;
    }


    @Override
    public List<CheckOutRecordEntry> getCheckOutRecord(String memberId) {
        HashMap<String, LibraryMember> libraryMemberHashMap = readMemberMap();
        LibraryMember libraryMember = libraryMemberHashMap.get(memberId);
        CheckOutRecord checkOutRecord = libraryMember.getCheckOutRecord();
        return checkOutRecord.getCheckOutRecordEntries();
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, Book> readBooksMap() {
        //Returns a Map with name/value pairs being
        //   isbn -> Book
        return (HashMap<String, Book>) readFromStorage(StorageType.BOOKS);
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, LibraryMember> readMemberMap() {
        //Returns a Map with name/value pairs being
        //   memberId -> LibraryMember
        return (HashMap<String, LibraryMember>) readFromStorage(
                StorageType.MEMBERS);
    }


    @SuppressWarnings("unchecked")
    public HashMap<String, User> readUserMap() {
        //Returns a Map with name/value pairs being
        //   userId -> User
        return (HashMap<String, User>) readFromStorage(StorageType.USERS);
    }


    static void loadBookMap(List<Book> bookList) {
        HashMap<String, Book> books = new HashMap<String, Book>();
        for (Book book : bookList) {
            books.put(book.getIsbn(), book);
        }
//		bookList.forEach(book -> books.put(book.getIsbn(), book));
        saveToStorage(StorageType.BOOKS, books);
    }


    static void loadUserMap(List<User> userList) {
        HashMap<String, User> users = new HashMap<String, User>();
        userList.forEach(user -> users.put(user.getId(), user));
        saveToStorage(StorageType.USERS, users);
    }

    static void loadMemberMap(List<LibraryMember> memberList) {
        HashMap<String, LibraryMember> members = new HashMap<String, LibraryMember>();
        memberList.forEach(member -> members.put(member.getMemberId(), member));
        saveToStorage(StorageType.MEMBERS, members);
    }

    static void saveToStorage(StorageType type, Object ob) {
        ObjectOutputStream out = null;
        try {
            Path path = FileSystems.getDefault().getPath(OUTPUT_DIR, type.toString());
            out = new ObjectOutputStream(Files.newOutputStream(path));
            out.writeObject(ob);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                }
            }
        }
    }

    static Object readFromStorage(StorageType type) {
        ObjectInputStream in = null;
        Object retVal = null;
        try {
            Path path = FileSystems.getDefault().getPath(OUTPUT_DIR, type.toString());
            in = new ObjectInputStream(Files.newInputStream(path));
            retVal = in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                }
            }
        }
        return retVal;
    }


    final static class Pair<S, T> implements Serializable {

        S first;
        T second;

        Pair(S s, T t) {
            first = s;
            second = t;
        }

        @Override
        public boolean equals(Object ob) {
            if (ob == null) return false;
            if (this == ob) return true;
            if (ob.getClass() != getClass()) return false;
            @SuppressWarnings("unchecked")
            Pair<S, T> p = (Pair<S, T>) ob;
            return p.first.equals(first) && p.second.equals(second);
        }

        @Override
        public int hashCode() {
            return first.hashCode() + 5 * second.hashCode();
        }

        @Override
        public String toString() {
            return "(" + first.toString() + ", " + second.toString() + ")";
        }

        private static final long serialVersionUID = 5399827794066637059L;
    }

}
