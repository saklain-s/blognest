import React, { useEffect } from 'react';
import { Container, Typography, Box, Paper, Chip, Divider } from '@mui/material';
import { useParams } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { fetchBlogById } from '../store/slices/blogSlice';

const BlogDetail = () => {
  const { id } = useParams();
  const dispatch = useDispatch();
  const { currentBlog, loading, error } = useSelector((state) => state.blog);

  useEffect(() => {
    if (id) {
      dispatch(fetchBlogById(id));
    }
  }, [dispatch, id]);

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

  if (!currentBlog) {
    return (
      <Container>
        <Typography>Blog not found</Typography>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg">
      <Box sx={{ py: 4 }}>
        <Paper elevation={2} sx={{ p: 4 }}>
          <Typography variant="h2" component="h1" gutterBottom>
            {currentBlog.title}
          </Typography>
          
          <Box sx={{ mb: 3 }}>
            <Typography variant="body1" color="text.secondary" gutterBottom>
              By {currentBlog.author} • {new Date(currentBlog.createdAt).toLocaleDateString()}
            </Typography>
            
            {currentBlog.tags && currentBlog.tags.length > 0 && (
              <Box sx={{ mt: 1 }}>
                {currentBlog.tags.map((tag) => (
                  <Chip key={tag} label={tag} size="small" sx={{ mr: 1, mb: 1 }} />
                ))}
              </Box>
            )}
          </Box>

          <Divider sx={{ mb: 3 }} />

          <Typography variant="body1" paragraph sx={{ lineHeight: 1.8 }}>
            {currentBlog.content}
          </Typography>

          {currentBlog.category && (
            <Box sx={{ mt: 3 }}>
              <Typography variant="body2" color="text.secondary">
                Category: <Chip label={currentBlog.category} size="small" />
              </Typography>
            </Box>
          )}
        </Paper>
      </Box>
    </Container>
  );
};

export default BlogDetail;
