export interface PagedResponse<T>{
    content: T[];
    page: number;
    size: number;
    totalElement: number;
    totalPages: number;
    first: boolean;
    last: boolean;
}