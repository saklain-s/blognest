import React, { useEffect, useState } from 'react';
import { Container, Typography, Box, Card, CardContent, CardActions, Button, Grid, TextField } from '@mui/material';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { fetchBlogs } from '../store/slices/blogSlice';

const BlogList = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { blogs, loading, error } = useSelector((state) => state.blog);

  useEffect(() => {
    dispatch(fetchBlogs({ page: 1, limit: 10, search: searchTerm }));
  }, [dispatch, searchTerm]);

  const handleSearch = (e) => {
    setSearchTerm(e.target.value);
  };

  if (loading) {
    return (
      <Container>
        <Typography>Loading...</Typography>
      </Container>
    );
  }

  if (error) {
    return (
      <Container>
        <Typography color="error">{error}</Typography>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg">
      <Box sx={{ py: 4 }}>
        <Typography variant="h3" component="h1" gutterBottom>
          Blog Posts
        </Typography>
        
        <TextField
          fullWidth
          label="Search blogs..."
          value={searchTerm}
          onChange={handleSearch}
          sx={{ mb: 4 }}
        />

        <Grid container spacing={3}>
          {blogs.map((blog) => (
            <Grid item xs={12} md={6} lg={4} key={blog.id}>
              <Card>
                <CardContent>
                  <Typography variant="h5" component="h2" gutterBottom>
                    {blog.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" paragraph>
                    {blog.excerpt || blog.content.substring(0, 150)}...
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    By {blog.author} • {new Date(blog.createdAt).toLocaleDateString()}
                  </Typography>
                </CardContent>
                <CardActions>
                  <Button size="small" onClick={() => navigate(`/blogs/${blog.id}`)}>
                    Read More
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>

        {blogs.length === 0 && (
          <Box sx={{ textAlign: 'center', py: 4 }}>
            <Typography variant="h6" color="text.secondary">
              No blogs found
            </Typography>
          </Box>
        )}
      </Box>
    </Container>
  );
};

export default BlogList;
