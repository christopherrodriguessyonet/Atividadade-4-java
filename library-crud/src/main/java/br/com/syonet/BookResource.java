package br.com.syonet;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;
import io.quarkus.panache.common.Page;


@Path("/book")
public class BookResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Book> list(
            @QueryParam("size") Integer size,
            @QueryParam("index") Integer index,
            @QueryParam("title") String title,
            @QueryParam("author") String author,
            @QueryParam("year") Integer year) {
    
        if (index == null) index = 0;
        if (size == null) size = 10;
    
        String query = "";
        List<Object> params = new ArrayList<>();
        int paramIndex = 1;
    
        if (title != null && !title.isEmpty()) {
            query += "title like ?" + paramIndex;
            params.add("%" + title + "%");
            paramIndex++;
        }
        if (author != null && !author.isEmpty()) {
            if (!query.isEmpty()) query += " and ";
            query += "author like ?" + paramIndex;
            params.add("%" + author + "%");
            paramIndex++;
        }
        if (year != null) {
            if (!query.isEmpty()) query += " and ";
            query += "year = ?" + paramIndex;
            params.add(year);
        }
    
        List<Book> books;
        if (query.isEmpty()) {
            books = Book.findAll().page(new Page(index, size)).list();
        } else {
            books = Book.find(query, params.toArray()).page(new Page(index, size)).list();
        }
    
        // Verifica se a lista está vazia e lança um erro 404
        if (books.isEmpty()) {
            throw new WebApplicationException("Nenhum livro encontrado com os critérios fornecidos", 404);
        }
    
        return books;
    }
    
    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Book create(Book book) {
        book.persist();
        return book;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Book findById(@PathParam("id") Long id) {
        return Book.findById(id);
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Book update(@PathParam("id") Long id, Book updatedBook) {
        Book book = Book.findById(id);
        if (book == null) {
            throw new WebApplicationException("Livro com ID " + id + " não encontrado", 404);
        }
        book.title = updatedBook.title;
        book.author = updatedBook.author;
        book.year = updatedBook.year;
        return book;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public void delete(@PathParam("id") Long id) {
        Book book = Book.findById(id);
        if (book != null) {
            book.delete();
        } else {
            throw new WebApplicationException("Livro com ID " + id + " não encontrado", 404);
        }
    }
}
