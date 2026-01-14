/*
 * Copyright (c) 2003-2011, Simon Brown
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 *   - Neither the name of Pebble nor the names of its contributors may
 *     be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.sourceforge.pebble.index;

import net.sourceforge.pebble.domain.*;
import net.sourceforge.pebble.search.SearchException;
import net.sourceforge.pebble.search.SearchHit;
import net.sourceforge.pebble.search.SearchResults;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;

/**
 * Wraps up the functionality to index blog entries. This is really just
 * a convenient wrapper around Lucene.
 *
 * @author    Simon Brown
 */
public class SearchIndex {

  /** the log used by this class */
  private static final Log log = LogFactory.getLog(SearchIndex.class);

  private final Blog blog;

  public SearchIndex(Blog blog) {
    this.blog = blog;
  }

  /**
   * Clears the index.
   */
  public void clear() {
    File searchDirectory = new File(blog.getSearchIndexDirectory());
    if (!searchDirectory.exists()) {
      searchDirectory.mkdirs();
    }

    synchronized (blog) {
      try {
        Analyzer analyzer = getAnalyzer();
        Directory dir = FSDirectory.open(searchDirectory.toPath());
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(dir, config);
        writer.commit();
        writer.close();
        dir.close();
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    }
  }

  /**
   * Allows a collection of blog entries to be indexed.
   */
  public void indexBlogEntries(Collection<BlogEntry> blogEntries) {
    synchronized (blog) {
      try {
        Analyzer analyzer = getAnalyzer();
        File indexDir = new File(blog.getSearchIndexDirectory());
        Directory dir = FSDirectory.open(indexDir.toPath());
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        IndexWriter writer = new IndexWriter(dir, config);

        for (BlogEntry blogEntry : blogEntries) {
          index(blogEntry, writer);
        }

        writer.commit();
        writer.close();
        dir.close();
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    }
  }

  /**
   * Allows a collection of static pages to be indexed.
   */
  public void indexStaticPages(Collection<StaticPage> staticPages) {
    synchronized (blog) {
      try {
        Analyzer analyzer = getAnalyzer();
        File indexDir = new File(blog.getSearchIndexDirectory());
        Directory dir = FSDirectory.open(indexDir.toPath());
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        IndexWriter writer = new IndexWriter(dir, config);

        for (StaticPage staticPage : staticPages) {
          index(staticPage, writer);
        }

        writer.commit();
        writer.close();
        dir.close();
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    }
  }

  /**
   * Allows a single blog entry to be (re)indexed. If the entry is already
   * indexed, this method deletes the previous index before adding the new
   * one.
   *
   * @param blogEntry   the BlogEntry instance to index
   */
  public void index(BlogEntry blogEntry) {
    try {
      synchronized (blog) {
        // first delete the blog entry from the index (if it was there)
        unindex(blogEntry);

        Analyzer analyzer = getAnalyzer();
        File indexDir = new File(blog.getSearchIndexDirectory());
        Directory dir = FSDirectory.open(indexDir.toPath());
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        IndexWriter writer = new IndexWriter(dir, config);
        index(blogEntry, writer);
        writer.commit();
        writer.close();
        dir.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.error(e.getMessage(), e);
    }
  }

  /**
   * Allows a single static page to be (re)indexed. If the page is already
   * indexed, this method deletes the previous index before adding the new
   * one.
   *
   * @param staticPage    the StaticPage instance to index
   */
  public void index(StaticPage staticPage) {
    try {
      synchronized (blog) {
        // first delete the static page from the index (if it was there)
        unindex(staticPage);

        Analyzer analyzer = getAnalyzer();
        File indexDir = new File(blog.getSearchIndexDirectory());
        Directory dir = FSDirectory.open(indexDir.toPath());
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        IndexWriter writer = new IndexWriter(dir, config);
        index(staticPage, writer);
        writer.commit();
        writer.close();
        dir.close();
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  /**
   * Escapes special characters in a query string while preserving field:value syntax.
   *
   * @param queryString the query string to escape
   * @return the escaped query string
   */
  private String escapeQueryString(String queryString) {
    // Split on field separator but preserve it
    int colonIndex = queryString.indexOf(':');
    if (colonIndex > 0 && colonIndex < queryString.length() - 1) {
      String field = queryString.substring(0, colonIndex);
      String value = queryString.substring(colonIndex + 1);
      // Escape the value part only
      value = QueryParser.escape(value);
      return field + ":" + value;
    }
    // No field:value syntax, escape the whole string
    return QueryParser.escape(queryString);
  }

  /**
   * Gets the Analyzer implementation to use.
   *
   * @return  an Analyzer instance
   * @throws Exception
   */
  private Analyzer getAnalyzer() throws Exception {
    String analyzerClassName = blog.getLuceneAnalyzer();
    try {
      // Try Lucene 9.x package first (e.g., org.apache.lucene.analysis.core.SimpleAnalyzer)
      if (analyzerClassName.contains(".analysis.") && !analyzerClassName.contains(".core.")) {
        String lucene9ClassName = analyzerClassName.replace(".analysis.", ".analysis.core.");
        try {
          Class c = Class.forName(lucene9ClassName);
          return (Analyzer)c.newInstance();
        } catch (ClassNotFoundException cnfe) {
          // Fall through to try original name
        }
      }

      Class c = Class.forName(analyzerClassName);
      return (Analyzer)c.newInstance();
    } catch (ClassNotFoundException cnfe) {
      // Fall back to StandardAnalyzer if configured analyzer not found
      log.warn("Analyzer " + analyzerClassName + " not found, using StandardAnalyzer");
      return new StandardAnalyzer();
    }
  }

  /**
   * Removes the index for a single blog entry to be removed.
   *
   * @param blogEntry   the BlogEntry instance to be removed
   */
  public void unindex(BlogEntry blogEntry) {
    try {
      synchronized (blog) {
        log.debug("Attempting to delete index for " + blogEntry.getTitle());
        File indexDir = new File(blog.getSearchIndexDirectory());
        if (!indexDir.exists()) {
          return; // Nothing to delete if index doesn't exist
        }
        Directory dir = FSDirectory.open(indexDir.toPath());
        Analyzer analyzer = getAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        IndexWriter writer = new IndexWriter(dir, config);
        Term term = new Term("id", blogEntry.getId());
        long deleted = writer.deleteDocuments(term);
        log.debug("Deleted " + deleted + " document(s) from the index");
        writer.commit();
        writer.close();
        dir.close();
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  /**
   * Removes the index for a single blog entry to be removed.
   *
   * @param staticPage    the StaticPage instance to be removed
   */
  public void unindex(StaticPage staticPage) {
    try {
      synchronized (blog) {
        log.debug("Attempting to delete index for " + staticPage.getTitle());
        File indexDir = new File(blog.getSearchIndexDirectory());
        if (!indexDir.exists()) {
          return; // Nothing to delete if index doesn't exist
        }
        Directory dir = FSDirectory.open(indexDir.toPath());
        Analyzer analyzer = getAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        IndexWriter writer = new IndexWriter(dir, config);
        Term term = new Term("id", staticPage.getId());
        long deleted = writer.deleteDocuments(term);
        log.debug("Deleted " + deleted + " document(s) from the index");
        writer.commit();
        writer.close();
        dir.close();
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  /**
   * Helper method to index an individual blog entry.
   *
   * @param blogEntry   the BlogEntry instance to index
   * @param writer      the IndexWriter to index with
   */
  private void index(BlogEntry blogEntry, IndexWriter writer) {
    if (!blogEntry.isPublished()) {
      return;
    }

    try {
      log.debug("Indexing " + blogEntry.getTitle());
      Document document = new Document();
      if (blogEntry.getId() == null) {
        log.error("BlogEntry ID is null, cannot index");
        return;
      }
      document.add(new StringField("id", blogEntry.getId(), Field.Store.YES));
      if (blogEntry.getTitle() != null) {
        document.add(new TextField("title", blogEntry.getTitle(), Field.Store.YES));
      } else {
        document.add(new TextField("title", "", Field.Store.YES));
      }
      if (blogEntry.getSubtitle() != null) {
        document.add(new TextField("subtitle", blogEntry.getSubtitle(), Field.Store.YES));
      } else {
        document.add(new TextField("subtitle", "", Field.Store.YES));
      }
      if (blogEntry.getPermalink() != null) {
        document.add(new StringField("permalink", blogEntry.getPermalink(), Field.Store.YES));
      }
      if (blogEntry.getDate() != null) {
        document.add(new StoredField("date", blogEntry.getDate().getTime()));
      }
      if (blogEntry.getBody() != null) {
        document.add(new TextField("body", blogEntry.getBody(), Field.Store.NO));
      } else {
        document.add(new TextField("body", "", Field.Store.NO));
      }
      if (blogEntry.getTruncatedContent() != null) {
        document.add(new TextField("truncatedBody", blogEntry.getTruncatedContent(), Field.Store.YES));
      } else {
        document.add(new TextField("truncatedBody", "", Field.Store.YES));
      }

      if (blogEntry.getAuthor() != null) {
        document.add(new TextField("author", blogEntry.getAuthor(), Field.Store.YES));
      }

      // build up one large string with all searchable content
      // i.e. entry title, entry body and all response bodies
      StringBuffer searchableContent = new StringBuffer();
      searchableContent.append(blogEntry.getTitle());
      searchableContent.append(" ");
      searchableContent.append(blogEntry.getBody());

      for (Category category : blogEntry.getCategories()) {
        document.add(new TextField("category", category.getId(), Field.Store.YES));
      }

      for (Tag tag : blogEntry.getAllTags()) {
        document.add(new TextField("tag", tag.getName(), Field.Store.YES));
      }

      searchableContent.append(" ");
      Iterator it = blogEntry.getComments().iterator();
      while (it.hasNext()) {
        Comment comment = (Comment)it.next();
        if (comment.isApproved()) {
          searchableContent.append(comment.getBody());
          searchableContent.append(" ");
        }
      }
      it = blogEntry.getTrackBacks().iterator();
      while (it.hasNext()) {
        TrackBack trackBack = (TrackBack)it.next();
        if (trackBack.isApproved()) {
          searchableContent.append(trackBack.getExcerpt());
          searchableContent.append(" ");
        }
      }

      // join the title and body together to make searching on them both easier
      document.add(new TextField("blogEntry", searchableContent.toString(), Field.Store.NO));

      writer.addDocument(document);
    } catch (Exception e) {
      e.printStackTrace();
      log.error(e.getMessage(), e);
    }
  }
  /**
   * Helper method to index an individual blog entry.
   *
   * @param staticPage    the Page instance instance to index
   * @param writer      the IndexWriter to index with
   */
  private void index(StaticPage staticPage, IndexWriter writer) {
    try {
      log.debug("Indexing " + staticPage.getTitle());
      Document document = new Document();
      document.add(new StringField("id", staticPage.getId(), Field.Store.YES));
      if (staticPage.getTitle() != null) {
        document.add(new TextField("title", staticPage.getTitle(), Field.Store.YES));
      } else {
        document.add(new TextField("title", "", Field.Store.YES));
      }
      if (staticPage.getPermalink() != null) {
        document.add(new StringField("permalink", staticPage.getPermalink(), Field.Store.YES));
      }
      if (staticPage.getDate() != null) {
        document.add(new StoredField("date", staticPage.getDate().getTime()));
      }
      if (staticPage.getBody() != null) {
        document.add(new TextField("body", staticPage.getBody(), Field.Store.NO));
      } else {
        document.add(new TextField("body", "", Field.Store.NO));
      }
      if (staticPage.getTruncatedContent() != null) {
        document.add(new TextField("truncatedBody", staticPage.getTruncatedContent(), Field.Store.YES));
      } else {
        document.add(new TextField("truncatedBody", "", Field.Store.YES));
      }

      if (staticPage.getAuthor() != null) {
        document.add(new TextField("author", staticPage.getAuthor(), Field.Store.YES));
      }

      // build up one large string with all searchable content
      // i.e. entry title, entry body and all response bodies
      StringBuffer searchableContent = new StringBuffer();
      searchableContent.append(staticPage.getTitle());
      searchableContent.append(" ");
      searchableContent.append(staticPage.getBody());

      // join the title and body together to make searching on them both easier
      document.add(new TextField("blogEntry", searchableContent.toString(), Field.Store.NO));

      writer.addDocument(document);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  public SearchResults search(String queryString) throws SearchException {

    log.debug("Performing search : " + queryString);

    SearchResults searchResults = new SearchResults();
    searchResults.setQuery(queryString);

    if (queryString != null && queryString.length() > 0) {
      Directory dir = null;
      IndexReader reader = null;

      try {
        File indexDir = new File(blog.getSearchIndexDirectory());
        if (!indexDir.exists()) {
          return searchResults; // Empty results if no index exists
        }
        dir = FSDirectory.open(indexDir.toPath());
        reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        QueryParser parser = new QueryParser("blogEntry", getAnalyzer());

        // Parse the query, escaping if needed
        Query query = null;
        try {
          query = parser.parse(queryString);
        } catch (ParseException pe) {
          // If parsing fails, try escaping special characters and parse again
          String escapedQuery = escapeQueryString(queryString);
          query = parser.parse(escapedQuery);
        }
        TopDocs topDocs = searcher.search(query, 1000); // Max 1000 results

        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
          Document doc = searcher.doc(scoreDoc.doc);
          Date date = null;
          if (doc.getField("date") != null && doc.getField("date").numericValue() != null) {
            long dateMillis = doc.getField("date").numericValue().longValue();
            date = new Date(dateMillis);
          }
          SearchHit result = new SearchHit(
              blog,
              doc.get("id"),
              doc.get("permalink"),
              doc.get("title"),
              doc.get("subtitle"),
              doc.get("truncatedBody"),
              date,
              scoreDoc.score);
          searchResults.add(result);
        }
      } catch (org.apache.lucene.index.IndexNotFoundException infe) {
        // Index doesn't exist yet - return empty results
      } catch (ParseException pe) {
        pe.printStackTrace();
        searchResults.setMessage("Sorry, but there was an error. Please try another search");
      } catch (Exception e) {
        e.printStackTrace();
        throw new SearchException(e.getMessage());
      } finally {
        if (reader != null) {
          try {
            reader.close();
          } catch (IOException e) {
            // can't do much now! ;-)
          }
        }
        if (dir != null) {
          try {
            dir.close();
          } catch (IOException e) {
            // can't do much now! ;-)
          }
        }
      }
    }

    return searchResults;
  }

}

