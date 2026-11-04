/** ErrorResponseDTO: formato estándar de error de la API. */
export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
}
