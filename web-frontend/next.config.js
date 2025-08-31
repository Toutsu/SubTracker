/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  // Убираем swcMinify, так как это вызывает предупреждение
  experimental: {
    reactCompiler: true,
  },
}

module.exports = nextConfig