package com.dhsdevelopments.potato.imagecache;

class CacheKey
{
    private String url;
    private boolean fromApi;

    public CacheKey( String url, boolean fromApi ) {
        this.url = url;
        this.fromApi = fromApi;
    }

    @SuppressWarnings( "SimplifiableIfStatement" )
    @Override
    public boolean equals( Object o ) {
        if( this == o ) {
            return true;
        }
        if( o == null || getClass() != o.getClass() ) {
            return false;
        }

        CacheKey cacheKey = (CacheKey)o;

        if( fromApi != cacheKey.fromApi ) {
            return false;
        }
        return url.equals( cacheKey.url );

    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + (fromApi ? 1 : 0);
        return result;
    }
}
