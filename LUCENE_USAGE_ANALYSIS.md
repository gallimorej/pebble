# Lucene Usage Analysis - Complete Scope

## Executive Summary

Lucene 1.4.1 is used in **1 primary class** with usage spread across **8 supporting files**. The implementation is well-encapsulated through the `SearchIndex` class, which acts as a facade. This is **good news** for migration - you'll primarily work in one place.

---

## Primary Implementation (Direct Lucene Usage)

### 1. `/src/main/java/net/sourceforge/pebble/index/SearchIndex.java` ⭐ **MAIN FILE**
**Lines of Code**: ~412 lines
**Lucene Imports**: 13 classes from org.apache.lucene.*

This is the **core search implementation** - the only file with direct Lucene API calls.

#### Lucene Classes Used:
```java
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DateField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
```

#### Key Methods That Use Lucene:
1. **`clear()`** (lines 80-95)
   - Creates new IndexWriter
   - Initializes empty search index

2. **`indexBlogEntries(Collection<BlogEntry>)`** (lines 100-115)
   - Bulk indexing of blog entries
   - Creates IndexWriter, adds documents

3. **`indexStaticPages(Collection<StaticPage>)`** (lines 120-135)
   - Bulk indexing of static pages

4. **`index(BlogEntry)`** (lines 144-158)
   - Single blog entry indexing
   - Creates IndexWriter, adds document

5. **`index(StaticPage)`** (lines 167-181)
   - Single static page indexing

6. **`index(BlogEntry, IndexWriter)`** (lines 238-314) - **PRIVATE**
   - Core indexing logic for blog entries
   - Creates Lucene Document
   - Adds Fields: id, title, subtitle, permalink, date, body, truncatedBody, author, category, tag
   - Indexes comments and trackbacks

7. **`index(StaticPage, IndexWriter)`** (lines 321-362) - **PRIVATE**
   - Core indexing logic for static pages
   - Similar field structure

8. **`unindex(BlogEntry)`** (lines 199-211)
   - Removes blog entry from index
   - Uses IndexReader to delete by Term

9. **`unindex(StaticPage)`** (lines 218-230)
   - Removes static page from index

10. **`search(String queryString)`** (lines 364-410) - **MAIN SEARCH**
    - Creates IndexSearcher
    - Parses query with QueryParser
    - Returns SearchResults with hits

11. **`getAnalyzer()`** (lines 189-192) - **PRIVATE**
    - Dynamically loads Lucene Analyzer based on configuration
    - Uses reflection to instantiate analyzer class

#### Document Schema (Fields Indexed):
```
Blog Entries:
- id (Keyword) - unique identifier
- title (Text) - blog entry title
- subtitle (Text) - blog entry subtitle
- permalink (Keyword) - permanent URL
- date (UnIndexed) - publication date
- body (UnStored) - full content body
- truncatedBody (Text) - excerpt/preview
- author (Text) - author name
- category (Text, multiple) - categories
- tag (Text, multiple) - tags
- blogEntry (UnStored) - combined searchable content (title + body + comments + trackbacks)

Static Pages:
- id (Keyword)
- title (Text)
- permalink (Keyword)
- date (UnIndexed)
- body (UnStored)
- truncatedBody (Text)
- author (Text)
- blogEntry (UnStored) - combined searchable content
```

---

## Supporting Files (Indirect Usage)

These files call `SearchIndex` but don't directly use Lucene APIs:

### 2. `/src/main/java/net/sourceforge/pebble/index/SearchIndexListener.java`
**Purpose**: Event listener that keeps search index synchronized with blog changes
**Lines**: 110 lines
**Calls SearchIndex**:
- `blogEntry.getBlog().getSearchIndex().index(blogEntry)` (line 107)
- `blogEntry.getBlog().getSearchIndex().unindex(blogEntry)` (lines 66, 100)

**Events Handled**:
- `blogEntryAdded()` - indexes new entries
- `blogEntryRemoved()` - removes from index
- `blogEntryChanged()` - reindexes
- `blogEntryPublished()` - indexes
- `blogEntryUnpublished()` - removes

---

### 3. `/src/main/java/net/sourceforge/pebble/web/action/SearchAction.java`
**Purpose**: HTTP request handler for search queries
**Lines**: 130 lines
**Calls SearchIndex**:
- `blog.getSearchIndex().search(query)` (line 96)

**Functionality**:
- Receives search query from HTTP request
- Calls SearchIndex to perform search
- Handles pagination (20 results per page)
- Sorts by relevance or date
- Special case: redirects directly if only 1 result

---

### 4. `/src/main/java/net/sourceforge/pebble/web/action/AdvancedSearchAction.java`
**Purpose**: Builds Lucene query syntax from advanced search form
**Lines**: 102 lines
**No Direct SearchIndex Calls** (builds query string, forwards to SearchAction)

**Functionality**:
- Constructs Lucene query syntax: `title:foo AND body:bar AND category:tech`
- Builds field-specific queries for:
  - `title:` - search in titles
  - `body:` - search in body content
  - `category:` - filter by categories
  - `author:` - filter by author
  - `tag:` - filter by tags

---

### 5. `/src/main/java/net/sourceforge/pebble/webservice/SearchAPIHandler.java`
**Purpose**: XML-RPC API for programmatic search access
**Calls SearchIndex**:
- `blog.getSearchIndex().search(searchString)` (lines 101, 145)

**API Methods**:
- `metaWeblog.searchPosts` - searches blog posts

---

### 6. `/src/main/java/net/sourceforge/pebble/service/StaticPageService.java`
**Purpose**: Service layer for managing static pages
**Calls SearchIndex**:
- `staticPage.getBlog().getSearchIndex().index(staticPage)` (line 159)
- `staticPage.getBlog().getSearchIndex().unindex(staticPage)` (line 183)

**Operations**:
- Indexes pages after creation/update
- Removes from index on deletion

---

### 7. `/src/main/java/net/sourceforge/pebble/domain/Blog.java`
**Purpose**: Main Blog domain object
**Lines**: ~1600+ lines
**Lucene-Related Code**:

```java
// Line 94 - Import
import net.sourceforge.pebble.index.SearchIndex;

// Line 128 - Configuration constant
public static final String LUCENE_ANALYZER_KEY = "luceneAnalyzer";

// Line 186 - SearchIndex instance variable
private SearchIndex searchIndex;

// Line 256 - Initialization
searchIndex = new SearchIndex(this);

// Line 792 - Getter for analyzer configuration
public String getLuceneAnalyzer() {
  return properties.getProperty(LUCENE_ANALYZER_KEY);
}

// Has method: getSearchIndexDirectory() (in parent AbstractBlog.java)
// Has method: getSearchIndex() - returns SearchIndex instance
```

**Analyzer Configuration**:
The blog can be configured with different Lucene analyzers (default is likely StandardAnalyzer).

---

### 8. `/src/main/java/net/sourceforge/pebble/domain/AbstractBlog.java`
**Purpose**: Base class for Blog
**Lucene-Related Code**:

```java
// Line 426
public String getSearchIndexDirectory() {
  return getRoot() + File.separator + "search";
}
```

Returns path where Lucene stores index files (typically: `/path/to/blog/search/`)

---

### 9. `/src/main/java/net/sourceforge/pebble/plugins/AvailablePlugins.java`
**Purpose**: Plugin management
**Lucene-Related Code**:

```java
// Line 102
public Collection<Plugin> getLuceneAnalyzers() {
  // Returns available Lucene Analyzer plugins
}
```

Provides list of available Lucene analyzers that can be configured.

---

## Test Files

### 10. `/src/test/java/net/sourceforge/pebble/index/SearchIndexTest.java`
**Purpose**: Unit tests for SearchIndex
**Lines**: ~200+ lines
**Test Coverage**:
- Tests indexing blog entries
- Tests searching
- Tests deletion from index
- Validates search results

---

## Configuration Files

### 11. Blog Properties
**Location**: Stored per-blog in `blog.properties`
**Lucene Configuration**:
```properties
luceneAnalyzer=org.apache.lucene.analysis.standard.StandardAnalyzer
```

Can be configured to use different analyzers:
- StandardAnalyzer (default)
- SimpleAnalyzer
- StopAnalyzer
- Various language-specific analyzers

---

## JSP Views (UI Only)

### 12. `/src/main/webapp/WEB-INF/jsp/viewPlugins.jsp`
**Lines 86-87**: Displays current Lucene analyzer configuration in admin UI

### 13. `/src/main/webapp/WEB-INF/jsp/aboutBlog.jsp`
**Line 179**: Shows which analyzer is configured

---

## Data Files & Documentation

### 14. `/pom.xml`
**Lines 129-132**: Maven dependency declaration
```xml
<dependency>
    <groupId>lucene</groupId>
    <artifactId>lucene</artifactId>
    <version>1.4.1</version>
</dependency>
```

### 15. `/build.xml`
**Line 148**: Ant build references `lucene-1.4.1.jar`

### 16-18. Documentation files (changelog.txt, upgrading2x.apt, help files)
Mention Lucene in upgrade notes and documentation

---

## Migration Impact Analysis

### Files That Need to Change for Elasticsearch Migration:

#### **Critical Changes** (Must Rewrite):
1. ✅ **SearchIndex.java** - Complete rewrite (~400 lines)
   - Replace all Lucene imports with Elasticsearch client
   - Rewrite indexing methods
   - Rewrite search method
   - Change field mapping approach

#### **Moderate Changes** (Update API Calls):
2. ⚠️ **SearchIndexListener.java** - Minimal changes
   - API calls remain the same (just calling SearchIndex methods)
   - Should work with minimal changes

3. ⚠️ **SearchAction.java** - Minimal changes
   - Already calls `blog.getSearchIndex().search(query)`
   - Query syntax may need changes

4. ⚠️ **AdvancedSearchAction.java** - Moderate changes
   - Builds Lucene query syntax (field:value AND field:value)
   - Need to convert to Elasticsearch Query DSL or continue using query string

5. ⚠️ **SearchAPIHandler.java** - Minimal changes
   - Just calls search method

6. ⚠️ **StaticPageService.java** - Minimal changes
   - Just calls index/unindex methods

7. ⚠️ **Blog.java** - Minor changes
   - May need to store Elasticsearch connection info instead of analyzer
   - `getSearchIndexDirectory()` may become irrelevant (ES stores remotely)

#### **Configuration Changes**:
8. ⚠️ **pom.xml** / **build.xml** - Update dependencies
   - Remove Lucene 1.4.1
   - Add Elasticsearch Java client

9. ⚠️ **Blog properties** - Update configuration
   - Replace `luceneAnalyzer` with Elasticsearch settings
   - Add ES connection details (host, port, index name)

#### **Optional Changes**:
10. ⚠️ **AvailablePlugins.java** - May remove analyzer plugins
11. ⚠️ **SearchIndexTest.java** - Rewrite tests for Elasticsearch
12. ⚠️ **JSP views** - Update admin UI to show ES connection info instead of analyzer

---

## Elasticsearch Migration Strategy

### Approach: Adapter Pattern

Keep the `SearchIndex` interface the same, but change the implementation:

```java
// Current (Lucene 1.4.1)
SearchIndex uses:
- IndexWriter → writes to local file system
- IndexSearcher → searches local index
- Document/Field → Lucene data structures

// Future (Elasticsearch)
SearchIndex uses:
- RestHighLevelClient → HTTP requests to ES
- IndexRequest → index documents to ES
- SearchRequest/SearchSourceBuilder → query ES
- XContentBuilder → JSON document building
```

### Migration Phases:

**Phase 1**: Core Implementation (2-3 weeks)
- Rewrite SearchIndex.java to use Elasticsearch Java client
- Maintain same public API (index, unindex, search methods)
- Map Lucene Document fields to Elasticsearch JSON documents
- Convert Lucene query syntax to ES Query DSL

**Phase 2**: Configuration (1 week)
- Add Elasticsearch connection configuration
- Update pom.xml dependencies
- Update initialization code in Blog.java

**Phase 3**: Testing (1-2 weeks)
- Rewrite SearchIndexTest.java
- Integration testing with embedded Elasticsearch (for tests)
- Manual testing of search functionality

**Phase 4**: Data Migration (1 week)
- Write migration script to reindex all blog entries
- Test with production data
- Document reindexing procedure

**Phase 5**: Deployment (1 week)
- Set up Elasticsearch infrastructure
- Deploy updated application
- Perform full reindex
- Monitor and tune

---

## The Good News 🎉

**Lucene usage is well-encapsulated!**

The fact that Lucene is only directly used in `SearchIndex.java` means:
- ✅ Changes are localized to one file
- ✅ Clean separation of concerns
- ✅ Existing API can be preserved
- ✅ Other components don't need major changes
- ✅ Can be swapped out cleanly

**Total LOC to rewrite**: ~400 lines (just SearchIndex.java)

**Total files to modify**: 8-10 files (mostly minor changes)

**Complexity**: Moderate - The architecture is already good, just need to swap the implementation.

---

## Estimated Effort Summary

| Task | Files | Effort |
|------|-------|--------|
| Core Implementation | SearchIndex.java | 2-3 weeks |
| Supporting Code | 6 Java files | 3-5 days |
| Configuration | pom.xml, Blog.java, properties | 2-3 days |
| Testing | SearchIndexTest.java + manual | 1-2 weeks |
| Data Migration | Scripts + procedures | 3-5 days |
| Infrastructure Setup | Elasticsearch deployment | 3-5 days |
| **Total** | **~10 files** | **6-8 weeks** |

This aligns with the original estimate in LUCENE_MIGRATION_OPTIONS.md!

---

## Next Steps

If you proceed with Elasticsearch migration:

1. **Set up Elasticsearch** (local for development)
2. **Create new branch** for Elasticsearch migration
3. **Start with SearchIndex.java** - rewrite incrementally:
   - Add Elasticsearch client dependency
   - Implement indexing first
   - Then implement search
   - Run tests at each step
4. **Update configuration** - add ES connection settings
5. **Create migration script** - to reindex existing content
6. **Test thoroughly** - compare search results with current implementation
7. **Document** - ES setup, configuration, maintenance procedures

Would you like me to create a detailed implementation plan or start with specific parts of the migration?
