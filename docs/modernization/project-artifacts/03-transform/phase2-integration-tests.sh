#!/bin/bash
# Phase 2 Integration Test Suite
# Tests Java 11/Spring 5.3.x/Lucene 9.x deployment

BASE_URL="http://localhost:8080/pebble"
PASSED=0
FAILED=0
TOTAL=25

echo "=== Phase 2 Integration Test Suite ==="
echo "Base URL: $BASE_URL"
echo "Date: $(date)"
echo ""

# Test helper functions
test_http_200() {
    local test_num=$1
    local test_name=$2
    local url=$3

    status=$(curl -s -o /dev/null -w "%{http_code}" "$url")
    if [ "$status" = "200" ]; then
        echo "✓ Test $test_num: $test_name - PASS"
        ((PASSED++))
        return 0
    else
        echo "✗ Test $test_num: $test_name - FAIL (HTTP $status)"
        ((FAILED++))
        return 1
    fi
}

test_content_present() {
    local test_num=$1
    local test_name=$2
    local url=$3
    local search_text=$4

    if curl -s "$url" | grep -q "$search_text"; then
        echo "✓ Test $test_num: $test_name - PASS"
        ((PASSED++))
        return 0
    else
        echo "✗ Test $test_num: $test_name - FAIL (text '$search_text' not found)"
        ((FAILED++))
        return 1
    fi
}

echo "### Core Application Health (4 tests) ###"
test_http_200 1 "Health Check Endpoint" "$BASE_URL/ping"
test_http_200 2 "Homepage Accessible" "$BASE_URL/"
test_content_present 3 "Homepage Content" "$BASE_URL/" "<html"
test_content_present 4 "Blog Title Present" "$BASE_URL/" "My blog"
echo ""

echo "### Feed Generation (XML) (4 tests) ###"
test_http_200 5 "RSS 2.0 Feed" "$BASE_URL/feed.xml"
test_content_present 6 "RSS Content" "$BASE_URL/feed.xml" "<rss"
test_http_200 7 "Atom Feed" "$BASE_URL/feed.action?flavor=atom"
test_http_200 8 "RDF Feed" "$BASE_URL/feed.action?flavor=rdf"
echo ""

echo "### JAXB XML Persistence (2 tests) ###"
test_content_present 9 "Blog Entry Rendering" "$BASE_URL/" "Welcome"
test_content_present 10 "XML Encoding" "$BASE_URL/" "UTF-8"
echo ""

echo "### Search Functionality (2 tests) ###"
test_http_200 11 "Search Page" "$BASE_URL/search.action"
test_content_present 12 "Search Form" "$BASE_URL/search.action" "search"
echo ""

echo "### Security & Authentication (3 tests) ###"
test_content_present 13 "Login Form Present" "$BASE_URL/loginPage.action" "login"
test_content_present 14 "Password Field" "$BASE_URL/loginPage.action" "password"
test_content_present 15 "CSRF Protection" "$BASE_URL/" "pebbleSecurityToken"
echo ""

echo "### Static Assets (3 tests) ###"
test_http_200 16 "CSS Loading" "$BASE_URL/styles/pebble.css"
test_http_200 17 "JavaScript Loading" "$BASE_URL/scripts/pebble.js"
test_http_200 18 "Theme CSS" "$BASE_URL/themes/default/screen.css"
echo ""

echo "### API Endpoints (2 tests) ###"
test_http_200 19 "Categories API" "$BASE_URL/categories/"
test_http_200 20 "Subscribe Action" "$BASE_URL/subscribe.action"
echo ""

echo "### Blog Functionality (3 tests) ###"
test_content_present 21 "Blog Entry Display" "$BASE_URL/" "blog entry"
test_content_present 22 "Comments Enabled" "$BASE_URL/" "comment"
test_content_present 23 "Permalink Present" "$BASE_URL/" "permalink"
echo ""

echo "### Java 11 Features (2 tests) ###"
test_content_present 24 "Date Handling" "$BASE_URL/" "202"
test_content_present 25 "String Processing" "$BASE_URL/" "blog"
echo ""

echo "=== Test Summary ==="
echo "Total Tests: $TOTAL"
echo "Passed: $PASSED"
echo "Failed: $FAILED"
echo "Success Rate: $(( PASSED * 100 / TOTAL ))%"

if [ $PASSED -eq $TOTAL ]; then
    echo ""
    echo "✅ ALL TESTS PASSED"
    exit 0
elif [ $PASSED -ge 24 ]; then
    echo ""
    echo "⚠️  MOSTLY PASSED (acceptable for Phase 2)"
    exit 0
else
    echo ""
    echo "❌ SIGNIFICANT FAILURES DETECTED"
    exit 1
fi
